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

package org.coodex.concrete.client.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.impl.AbstractSyncInvoker;
import org.coodex.concrete.common.*;
import org.coodex.concrete.dubbo.DubboHelper;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.coodex.concrete.common.ConcreteHelper.VERSION;
import static org.coodex.concrete.dubbo.DubboHelper.*;

public class DubboClientInvoker extends AbstractSyncInvoker {

    private final static String CLIENT_AGENT = "concrete-dubbo-client-" + VERSION;

    private final static Logger log = LoggerFactory.getLogger(DubboClientInvoker.class);
    @SuppressWarnings({"unsafe", "unchecked"})
    private static SingletonMap<DubboCacheKey, Object> dubboClientInstances =
            new SingletonMap<DubboCacheKey, Object>(new SingletonMap.Builder<DubboCacheKey, Object>() {
                @Override

                public Object build(DubboCacheKey key) {
//                    Destination destination = key.getDestination();
//                    String registries = getString(destination.getIdentify(), "registry");
//                    String application = getString(destination.getIdentify(), "name");
                    DubboDestination destination = key.getDestination();
                    ApplicationConfig applicationConfig = DubboHelper.applications.getInstance(
                            destination.getName() == null ? "concrete-dubbo-application" : destination.getName());
                    List<RegistryConfig> registryConfigs = buildRegistryConfigs(destination.getRegistries());
                    ReferenceConfig reference = new ReferenceConfig(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
                    reference.setApplication(applicationConfig);
                    reference.setRegistries(registryConfigs); // 多个注册中心可以用setRegistries()
                    reference.setInterface(getDubboInterface(key.clz));
                    // TODO setVersion
//                    String version =

                    return reference.get();
                }
            });


    public DubboClientInvoker(DubboDestination destination) {
        super(destination);
    }

    private static String mapToStr(Map<String, String> map) {
        StringBuilder buffer = new StringBuilder();
        for (String key : map.keySet()) {
            buffer.append("\n\t").append(key).append(": ").append(map.get(key));
        }
        return buffer.toString();
    }

    private static Map<String, String> subjoinToMap(Subjoin subjoin) {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        for (String key : subjoin.keySet()) {
            map.put(key, subjoin.get(key));
        }
        return map;
    }

    private static Method findMethod(Class concreteClass, Method method) {
        Class proxyClass = getDubboInterface(concreteClass);
        for (Method m : proxyClass.getMethods()) {
            if (m.getName().equalsIgnoreCase(method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), m.getParameterTypes()))
                return m;
        }
        throw new RuntimeException("method not found. " + method);
    }

    @Override
    protected Object execute(Class clz, Method method, Object[] args) throws Throwable {
        ClientSideContext context = (ClientSideContext) ConcreteContext.getServiceContext();
        String tokenId = ClientTokenManagement.getTokenId(getDestination(), context.getTokenId());
        if (!Common.isBlank(tokenId)) {
            RpcContext.getContext().setAttachment(Token.CONCRETE_TOKEN_ID_KEY, tokenId);
        }
        RpcContext.getContext().setAttachment(AGENT, CLIENT_AGENT);
        RpcContext.getContext().setAttachment(SUBJOIN,
                JSONSerializerFactory.getInstance().toJson(context.getSubjoin()));
        RpcContext.getContext().setAttachment(LOCATE, context.getLocale().toLanguageTag());
        if (log.isDebugEnabled()) {
            Map<String, String> map = subjoinToMap(context.getSubjoin());
            if (!Common.isBlank(tokenId))
                map.put(Token.CONCRETE_TOKEN_ID_KEY, tokenId);
            log.debug("subjoin before invoke: {}",
                    mapToStr(map));
        }

        Map<String, String> result = (Map<String, String>) findMethod(clz, method).invoke(
                dubboClientInstances.getInstance(new DubboCacheKey((DubboDestination) getDestination(), clz)),
                args);

        if (log.isDebugEnabled()) {
            log.debug("subjoin after invoke: {}", mapToStr(result));
        }

        tokenId = result.get(Token.CONCRETE_TOKEN_ID_KEY);
        if (!Common.isBlank(tokenId)) {
            ClientTokenManagement.setTokenId(getDestination(), tokenId);
        }
        String content = result.get(RESULT);
        return content == null ? null : JSONSerializerFactory.getInstance()
                .parse(content, TypeHelper.toTypeReference(
                        method.getGenericReturnType(), clz
                ));
    }

    @Override
    public ServiceContext buildContext(Class concreteClass, Method method) {
        return new DubboClientContext(getDestination(), RuntimeContext.getRuntimeContext(method, concreteClass));
    }

    static class DubboCacheKey {
        private final DubboDestination destination;
        private final Class clz;

        public DubboCacheKey(DubboDestination destination, Class clz) {
            this.destination = destination;
            this.clz = clz;
        }

        public DubboDestination getDestination() {
            return destination;
        }

        public Class getClz() {
            return clz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DubboCacheKey that = (DubboCacheKey) o;

            if (!destination.equals(that.destination)) return false;
            return clz.equals(that.clz);
        }

        @Override
        public int hashCode() {
            int result = destination.hashCode();
            result = 31 * result + clz.hashCode();
            return result;
        }
    }

}
