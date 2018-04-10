/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.support.dubbo;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import org.coodex.closure.CallableClosure;
import org.coodex.concrete.api.Application;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Common;
import org.coodex.util.GenericType;
import org.coodex.util.ReflectHelper;
import org.coodex.util.SingletonMap;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.coodex.concrete.common.AModule.getUnit;
import static org.coodex.concrete.dubbo.DubboHelper.*;

public class DubboApplication implements Application {



    private static SingletonMap<Integer, ProtocolConfig> protocals =
            new SingletonMap<Integer, ProtocolConfig>(
                    new SingletonMap.Builder<Integer, ProtocolConfig>() {
                        @Override
                        public ProtocolConfig build(Integer key) {
                            ProtocolConfig protocolConfig = new ProtocolConfig();
                            protocolConfig.setPort(key.intValue());
                            protocolConfig.setHost("0.0.0.0");
//                            protocolConfig.setName();
                            // TODO 确认是否需要
                            return protocolConfig;
                        }
                    }
            );


    private static class DubboCaller implements Caller {
        public static final String UNKNOWN = "unknown";

        private final String address;
        private final String agent;

        public DubboCaller(String address, String agent) {
            this.address = Common.isBlank(address) ? UNKNOWN : address;
            this.agent = Common.isBlank(agent) ? UNKNOWN : agent;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getAgent() {
            return agent;
        }
    }


    private static class InvokerBuilder implements SingletonMap.Builder<Class, Object> {
        @Override
        public Object build(Class key) {
            if (ConcreteHelper.isConcreteService(key)) {
                return buildConcreteServiceImpl(key);
            } else {
                throw new RuntimeException(key + " NOT compatible class.");
            }
        }


        private Object buildConcreteServiceImpl(final Class concreteClass) {

            return Proxy.newProxyInstance(DubboApplication.class.getClassLoader(),
                    new Class[]{concreteClass}, new InvocationHandler() {

                        private Method findActualMethod(Method methodOfProxy) throws NoSuchMethodException {
                            return methodOfProxy.getParameterTypes().length == 0 ?
                                    concreteClass.getMethod(methodOfProxy.getName()) :
                                    concreteClass.getMethod(methodOfProxy.getName(), methodOfProxy.getParameterTypes());
                        }

                        @Override
                        public Object invoke(Object o, Method method, final Object[] objects) throws Throwable {
                            if (method.getDeclaringClass().equals(Object.class)) {
                                return method.invoke(this, objects);
                            }
                            final Method m = findActualMethod(method);
                            String clientIP = RpcContext.getContext().getRemoteHost();
                            Map<String, String> map = JSONSerializerFactory.getInstance().parse(
                                    RpcContext.getContext().getAttachment(SUBJOIN),
                                    new GenericType<Map<String, String>>() {
                                    }.genericType());
                            SubjoinBaseJava7 subjoin = new SubjoinBaseJava7(map);
                            String tokenId = RpcContext.getContext().getAttachment(Token.CONCRETE_TOKEN_ID_KEY);
                            String agent = RpcContext.getContext().getAttachment(AGENT);
                            RpcContext.getContext().removeAttachment(Token.CONCRETE_TOKEN_ID_KEY)
                                    .removeAttachment(AGENT).removeAttachment(SUBJOIN);
                            try {
                                Object result = ConcreteContext.runWithContext(
                                        new DubboServiceContext(
                                                new DubboCaller(clientIP, agent), getUnit(concreteClass, m),
                                                subjoin,
                                                tokenId == null ? null : BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(tokenId)
                                        ),
                                        new CallableClosure() {
                                            @Override
                                            public Object call() throws Throwable {
                                                return m.invoke(BeanProviderFacade.getBeanProvider()
                                                        .getBean(concreteClass), objects);
                                            }
                                        });
                                try {
                                    String newTokenId = TokenWrapper.getInstance().getTokenId();
                                    if (!Common.isBlank(newTokenId) && !Common.isSameStr(newTokenId, tokenId)) {
                                        RpcContext.getContext().setAttachment(Token.CONCRETE_TOKEN_ID_KEY, newTokenId);
                                    }
                                } catch (Throwable th) {
                                }
                                RpcContext.getContext().setAttachment(SUBJOIN,
                                        JSONSerializerFactory.getInstance().toJson(subjoin.toMap()));
                                // todo 确认是否回传
                                return result;
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    });

        }

    }


    private final String name;
    private final List<RegistryConfig> registryConfig;
    private final int[] ports;

    public DubboApplication(String name, List<RegistryConfig> registryConfig, int... ports) {
        this.name = name;
        this.ports = ports == null || ports.length == 0 ? new int[]{-1} : ports;
        if (registryConfig == null || registryConfig.size() == 0) {
            throw new RuntimeException("no registry for " + name);
        }
        this.registryConfig = registryConfig;
    }


    @Override
    public void registerPackage(String... packages) {
        ConcreteHelper.foreachService(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                registerClass(serviceClass);
            }
        }, packages);
    }

    @Override
    public void register(Class<?>... classes) {
        for (Class<?> clz : classes)
            registerClass(clz);
    }

    private Set<Class<?>> registered = new ConcurrentHashSet<Class<?>>();

    private static final InvokerBuilder invokerBuilder = new InvokerBuilder();

    private void registerClass(Class<?> concreteClass) {
        IF.not(ConcreteHelper.isConcreteService(concreteClass), concreteClass + " NOT concrete service.");
        if (!registered.contains(concreteClass)) {

            ServiceConfig serviceConfig = new ServiceConfig();
            serviceConfig.setApplication(applications.getInstance(name));
            serviceConfig.setRegistries(registryConfig);
            List<ProtocolConfig> protocolConfigs = new ArrayList<ProtocolConfig>();
            for (int port : ports) {
                protocolConfigs.add(protocals.getInstance(port));
            }
            serviceConfig.setProtocols(protocolConfigs);
            serviceConfig.setInterface(concreteClass);
            serviceConfig.setRef(invokerBuilder.build(concreteClass));
            serviceConfig.export();
            registered.add(concreteClass);
        }

    }

}
