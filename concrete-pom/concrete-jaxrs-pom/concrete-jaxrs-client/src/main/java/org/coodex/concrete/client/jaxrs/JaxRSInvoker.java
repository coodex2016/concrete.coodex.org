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

package org.coodex.concrete.client.jaxrs;

import org.coodex.concrete.ClientException;
import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.ExceptionMapper;
import org.coodex.concrete.client.impl.AbstractSyncInvoker;
import org.coodex.concrete.common.*;
import org.coodex.concrete.jaxrs.logging.ClientLogger;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.mock.Mocker;
import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.ClientHelper.getSSLContext;
import static org.coodex.concrete.common.ConcreteHelper.isDevModel;
import static org.coodex.concrete.common.Token.CONCRETE_TOKEN_ID_KEY;
import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;
import static org.coodex.concrete.jaxrs.JaxRSHelper.getUnitFromContext;

public class JaxRSInvoker extends AbstractSyncInvoker {

    private static final ServiceLoader<ConfigurationProvider> CONFIGURATION_PROVIDER_SERVICE_LOADER
            = new LazyServiceLoader<ConfigurationProvider>((ConfigurationProvider) () -> null) {
    };

    private final static Logger log = LoggerFactory.getLogger(JaxRSInvoker.class);

    private final static SelectableServiceLoader<Throwable, ExceptionMapper> EXCEPTION_MAPPERS =
            new LazySelectableServiceLoader<Throwable, ExceptionMapper>() {
            };

    private final Client client;

    JaxRSInvoker(JaxRSDestination destination) {
        super(destination);
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        Configuration configuration = CONFIGURATION_PROVIDER_SERVICE_LOADER.get().getConfiguration();
        if (configuration != null) {
            clientBuilder = clientBuilder.withConfig(configuration);
        }
        if (destination.getConnectTimeout() != null && destination.getConnectTimeout() > 0) {
            clientBuilder = clientBuilder.connectTimeout(destination.getConnectTimeout(), TimeUnit.MILLISECONDS);
        }
        clientBuilder = clientBuilder.register(ClientLogger.class)
                .register(GZIPReaderInterceptor.class);
        client = destination.isSsl() ?
                clientBuilder.hostnameVerifier((s, sslSession) -> true).sslContext(getSSLContext(destination.getSsl())).build() : // NOSONAR
                clientBuilder.build();
    }

    private Invocation.Builder buildHeaders(Invocation.Builder builder/*, StringBuilder str*/, Subjoin subjoin, String tokenId) {
        JaxRSClientContext context = JaxRSClientCommon.getContext();
        builder = builder.acceptEncoding("gzip")
                .acceptLanguage(context.getLocale());
        if (subjoin != null || !Common.isBlank(tokenId)) {
            if (subjoin != null) {
                for (String key : subjoin.keySet()) {
                    builder = builder.header(key, subjoin.get(key));
                }
            }
            if (!Common.isBlank(tokenId)) {
                builder = builder.header(CONCRETE_TOKEN_ID_KEY, tokenId);
            }
        }
        return builder;
    }

    @Override
    protected Object execute(Class<?> clz, Method method, Object[] args) throws Throwable {
        JaxrsUnit unit = getUnitFromContext(ConcreteHelper.getContext(method, clz));
        if (isDevModel("jaxrs.client")) {
            return Mocker.mockMethod(
                    unit.getMethod(),
                    unit.getDeclaringModule().getInterfaceClass());
        } else {
            String path = JaxRSClientCommon.getPath(unit, args, (JaxRSDestination) getDestination());
            // 找需要提交的对象
            Object toSubmit = JaxRSClientCommon.getSubmitObject(unit, args);

            try (Response response = request(path, unit.getInvokeType(), toSubmit)) {

                String tokenId = response.getHeaderString(Token.CONCRETE_TOKEN_ID_KEY);
                ClientTokenManagement.setTokenId(getDestination(), tokenId);

                String body = response.readEntity(String.class);
                JaxRSClientCommon.handleResponseHeaders(response.getHeaders());
                return JaxRSClientCommon.processResult(response.getStatus(), body, unit,
                        response.getHeaders().containsKey(HEADER_ERROR_OCCURRED), path);
            } catch (ClientException clientEx) {
                throw clientEx;
            } catch (Throwable th) {
                ExceptionMapper mapper = EXCEPTION_MAPPERS.select(th);
                if (mapper != null) {
                    ClientException clientException = mapper.mapException(th);
                    if (clientException != null) {
                        throw clientException;
                    }
                }
                JaxRSClientException ce = new JaxRSClientException(-1, th.getLocalizedMessage(), path, unit.getInvokeType());
                ce.initCause(th);
                throw ce;
            }
        }
    }

    private Response request(String url, String method, Object body) {
        Invocation.Builder builder = getInvokerBuilder(url);

        return body == null ?
                builder.build(method).invoke() :
                builder.build(method, Entity.entity(body,
                        MediaType.APPLICATION_JSON_TYPE.withCharset(
                                JaxRSClientCommon.getEncodingCharset((JaxRSDestination) getDestination())
                        ))).invoke();
    }

    private Invocation.Builder getInvokerBuilder(String url) {
        Invocation.Builder builder = client.target(url).request();

        JaxRSClientContext context = JaxRSClientCommon.getContext();
        Subjoin subjoin = context.getSubjoin();
        String tokenId = ClientTokenManagement.getTokenId(getDestination(), context.getTokenId());

        return buildHeaders(builder, subjoin, tokenId);
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new JaxRSClientContext(getDestination(), context, "concrete-client-jaxrs");
    }

}
