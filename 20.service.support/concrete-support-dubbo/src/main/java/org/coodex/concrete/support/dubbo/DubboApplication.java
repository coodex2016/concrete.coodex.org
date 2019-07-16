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
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;
import org.coodex.concrete.common.*;
import org.coodex.concrete.dubbo.ProxyFor;
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;
import org.coodex.util.ReflectHelper;
import org.coodex.util.SingletonMap;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.coodex.concrete.common.ConcreteHelper.isConcreteService;
import static org.coodex.concrete.common.ConcreteHelper.updatedMap;
import static org.coodex.concrete.dubbo.DubboHelper.*;

public class DubboApplication implements Application {


    private static final InvokerBuilder invokerBuilder = new InvokerBuilder();
    private static SingletonMap<Integer, ProtocolConfig> protocals =
            new SingletonMap<>(
                    new SingletonMap.Builder<Integer, ProtocolConfig>() {
                        @Override
                        public ProtocolConfig build(Integer key) {
                            ProtocolConfig protocolConfig = new ProtocolConfig();
                            protocolConfig.setPort(key);
                            protocolConfig.setHost("0.0.0.0");
//                            protocolConfig.setName();
                            // TODO 确认是否需要
                            return protocolConfig;
                        }
                    }
            );
    private final String name;
    private final List<RegistryConfig> registryConfig;
    private final int[] ports;
    private Set<Class<?>> registered = new ConcurrentHashSet<>();

    public DubboApplication(String name, List<RegistryConfig> registryConfig, int... ports) {
        this.name = name;
        this.ports = ports == null || ports.length == 0 ? new int[]{-1} : ports;
        if (registryConfig == null || registryConfig.size() == 0) {
            throw new RuntimeException("no registry for " + name);
        }
        this.registryConfig = registryConfig;
    }

    private static String objectToStr(Object o) {
        return JSONSerializerFactory.getInstance().toJson(o);
    }

    @Override
    public void registerPackage(String... packages) {
        ConcreteHelper.foreachClassInPackages(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                _register(serviceClass);
            }
        }, packages);
    }

    @Override
    public void register(Class<?>... classes) {
        for (Class<?> clz : classes) {
            _register(clz);
        }
    }

    private void _register(Class<?> clazz) {
        if (isConcreteService(clazz)) {
            registerClass(clazz);
        } else if (AbstractErrorCodes.class.isAssignableFrom(clazz)) {
            //noinspection unchecked
            ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clazz);
        }

    }

    private void registerClass(Class<?> concreteClass) {
//        IF.not(ConcreteHelper.isConcreteService(concreteClass), concreteClass + " NOT concrete service.");

        concreteClass = getDubboInterface(concreteClass);
        if (!registered.contains(concreteClass)) {

            ServiceConfig serviceConfig = new ServiceConfig();
            serviceConfig.setApplication(applications.getInstance(name));
            serviceConfig.setRegistries(registryConfig);
            List<ProtocolConfig> protocolConfigs = new ArrayList<>();
            for (int port : ports) {
                protocolConfigs.add(protocals.getInstance(port));
            }
            serviceConfig.setProtocols(protocolConfigs);
            //noinspection unchecked
            serviceConfig.setInterface(concreteClass);
            //noinspection unchecked
            serviceConfig.setRef(invokerBuilder.build(concreteClass));
            serviceConfig.export();
            registered.add(concreteClass);
        }

    }

    private static class DubboCaller implements Caller {
        static final String UNKNOWN = "unknown";

        private final String address;
        private final String agent;

        DubboCaller(String address, String agent) {
            this.address = Common.isBlank(address) ? UNKNOWN : address;
            this.agent = Common.isBlank(agent) ? UNKNOWN : agent;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getClientProvider() {
            return agent;
        }
    }

    private static class InvokerBuilder implements SingletonMap.Builder<Class, Object> {
        @Override
        public Object build(Class key) {
            return buildConcreteServiceImpl(key);
        }


        @SuppressWarnings("unchecked")
        private Object buildConcreteServiceImpl(final Class dpClass) {

            final Class concreteClass = ((Class<?>) dpClass).getAnnotation(ProxyFor.class).value();

            return Proxy.newProxyInstance(DubboApplication.class.getClassLoader(),
                    new Class[]{dpClass}, new InvocationHandler() {

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
                                    new GenericTypeHelper.GenericType<Map<String, String>>() {
                                    }.getType());
                            Subjoin subjoin = new DubboSubjoin(map).wrap();
                            String locate = RpcContext.getContext().getAttachment(LOCATE);
                            String tokenId = RpcContext.getContext().getAttachment(Token.CONCRETE_TOKEN_ID_KEY);
                            String agent = RpcContext.getContext().getAttachment(AGENT);
                            ServerSideContext serverSideContext = new DubboServiceContext(
                                    new DubboCaller(clientIP, agent),
                                    subjoin,
                                    locate == null ? null : Locale.forLanguageTag(locate),
                                    tokenId
                            );
                            Trace trace = APM.build(serverSideContext.getSubjoin())
                                    .tag("remote", serverSideContext.getCaller().getAddress())
                                    .tag("agent", serverSideContext.getCaller().getClientProvider())
                                    .start(String.format("dubbo: %s.%s", method.getDeclaringClass().getName(), method.getName()));

                            try {
                                Object result = ConcreteContext.runServiceWithContext(
                                        serverSideContext,
                                        new CallableClosure() {
                                            @Override
                                            public Object call() throws Throwable {
                                                return m.invoke(BeanServiceLoaderProvider.getBeanProvider()
                                                        .getBean(concreteClass), objects);
                                            }
                                        }, concreteClass, m, objects);
                                Map<String, String> toClient = new ConcurrentHashMap<>();
                                Map<String, String> subjoinMap = updatedMap(subjoin);
                                if (subjoinMap.size() > 0)
                                    toClient.put(SUBJOIN, objectToStr(subjoinMap));

                                try {
                                    String newTokenId = serverSideContext.getTokenId();
                                    if (!Common.isBlank(newTokenId) && !Common.isSameStr(newTokenId, tokenId)) {
                                        toClient.put(Token.CONCRETE_TOKEN_ID_KEY, newTokenId);
                                    }
                                } catch (Throwable th) {
                                    // do nothing
                                }
                                if (result != null)
                                    toClient.put(RESULT, objectToStr(result));
                                return toClient;
                            } catch (Throwable th) {
                                trace.error(th);
                                throw th;
                            } finally {
                                trace.finish();
                            }
                        }
                    });

        }

    }

}
