/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.AbstractSyncInvoker;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.dubbo.ApacheDubboSubjoin;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static org.coodex.concrete.common.ConcreteHelper.*;
import static org.coodex.concrete.dubbo.DubboConfigCaching.getApplicationConfig;
import static org.coodex.concrete.dubbo.DubboConfigCaching.getRegistries;


public class ApacheDubboClientInvoker extends AbstractSyncInvoker {

    private final static Logger log = LoggerFactory.getLogger(ApacheDubboClientInvoker.class);
    private final static String CLIENT_AGENT = "concrete-apache-dubbo-client-" + VERSION;

    private static final SingletonMap<ReferenceKey, Object> REFERENCE_MAP = SingletonMap.<ReferenceKey, Object>builder()
            .function(key -> {
                ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
                referenceConfig.setApplication(getApplicationConfig(key.dubboDestination.getName()));
                referenceConfig.setRegistries(getRegistries(key.dubboDestination.getRegistries()));
                referenceConfig.setProtocol(key.dubboDestination.getProtocol());
                referenceConfig.setUrl(key.dubboDestination.getUrl());
                referenceConfig.setInterface(key.serviceClass);
                referenceConfig.setVersion(VERSION);
                return referenceConfig.get();
            }).build();

    public ApacheDubboClientInvoker(Destination destination) {
        super(destination);
    }

    @Override
    protected Object execute(Class<?> clz, Method method, Object[] args) throws Throwable {
        ApacheDubboDestination dubboDestination = (ApacheDubboDestination) getDestination();
        ClientSideContext context = (ClientSideContext) ConcreteContext.getServiceContext();
        String tokenId = ClientTokenManagement.getTokenId(getDestination(), context.getTokenId());


        RpcContext rpcContext = RpcContext.getContext();

        // copy subjoin
        Subjoin subjoin = context.getSubjoin();
        for (String key : subjoin.keySet()) {
            rpcContext.setAttachment(key, subjoin.get(key));
        }

        // agent
        rpcContext.setAttachment(AGENT_KEY, CLIENT_AGENT);

        // token
        if (!Common.isBlank(tokenId)) {
            rpcContext.setAttachment(TOKEN_KEY, tokenId);
        }
        Object ref = REFERENCE_MAP.get(new ReferenceKey(clz, dubboDestination));
        traceContext("before invoke:", rpcContext);
        Object result = (args == null || args.length == 0) ?
                method.invoke(ref) :
                method.invoke(ref, args);
        traceContext("after invoke:", rpcContext);
        context.responseSubjoin(new ApacheDubboSubjoin(rpcContext.getAttachments()));
        ClientTokenManagement.setTokenId(dubboDestination, rpcContext.getAttachment(TOKEN_KEY));
        return result;
    }

    private void traceContext(String label, RpcContext context) {
        if (log.isInfoEnabled()) {
            StringBuilder builder = new StringBuilder();
            Map<String, String> map = context.getAttachments();

            builder.append(map.size()).append(" context attachments");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                builder.append("\n\t").append(entry.getKey()).append(": ")
                        .append(entry.getValue());
            }

            log.info("{}: {}", label, builder);
        }
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new ApacheDubboClientContext(getDestination(), context);
    }

    static class ReferenceKey {
        private Class<?> serviceClass;
        private ApacheDubboDestination dubboDestination;

        public ReferenceKey(Class<?> serviceClass, ApacheDubboDestination dubboDestination) {
            this.serviceClass = serviceClass;
            this.dubboDestination = dubboDestination;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReferenceKey)) return false;

            ReferenceKey that = (ReferenceKey) o;

            if (!Objects.equals(serviceClass, that.serviceClass))
                return false;
            return Objects.equals(dubboDestination, that.dubboDestination);
        }

        @Override
        public int hashCode() {
            int result = serviceClass != null ? serviceClass.hashCode() : 0;
            result = 31 * result + (dubboDestination != null ? dubboDestination.hashCode() : 0);
            return result;
        }
    }
}
