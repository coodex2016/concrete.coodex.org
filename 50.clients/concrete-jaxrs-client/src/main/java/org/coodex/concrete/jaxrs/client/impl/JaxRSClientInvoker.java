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

package org.coodex.concrete.jaxrs.client.impl;

import org.coodex.concrete.client.ClientCommon;
import org.coodex.concrete.client.MessagePojo;
import org.coodex.concrete.client.MessageSubscriber;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.jaxrs.client.AbstractRemoteInvoker;
import org.coodex.concrete.jaxrs.client.JaxRSClientConfigBuilder;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.client.jaxrs.JaxRSInvoker.buildHeaders;
import static org.coodex.concrete.common.ConcreteContext.getServiceContext;
import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;

/**
 * Created by davidoff shen on 2017-04-15.
 */
@Deprecated
public class JaxRSClientInvoker extends AbstractRemoteInvoker {

    private final static Logger log = LoggerFactory.getLogger(JaxRSClientInvoker.class);


    private static final Map<String, Client> clients = new HashMap<String, Client>();

    private final static Map<String, AbstractCookieManager> DOMAIN_COOKIE_MANAGERS =
            new HashMap<String, AbstractCookieManager>();

    public final static AbstractCookieManager getCookieManager(String domain) {
        synchronized (DOMAIN_COOKIE_MANAGERS) {
            if (!DOMAIN_COOKIE_MANAGERS.keySet().contains(domain)) {
                DOMAIN_COOKIE_MANAGERS.put(domain, new AbstractCookieManager() {
                });
            }
        }
        return DOMAIN_COOKIE_MANAGERS.get(domain);
    }


    private final Client client;


    private static final ServiceLoader<JaxRSClientConfigBuilder> BUILDER_SPI_FACADE =
            new ConcreteServiceLoader<JaxRSClientConfigBuilder>() {
                @Override
                public JaxRSClientConfigBuilder getConcreteDefaultProvider() {
                    return new JaxRSClientConfigBuilder() {
                        @Override
                        public Configuration buildConfig() {
                            return null;
                        }
                    };
                }
            };

    private static Client getClient(String domain, SSLContext context, Configuration configuration) {
        synchronized (clients) {
            if (!clients.keySet().contains(domain)) {
                ClientBuilder clientBuilder = ClientBuilder.newBuilder();
                if (context != null) {
                    clientBuilder = clientBuilder.hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }).sslContext(context);
                }

                if (configuration != null) {
                    clientBuilder = clientBuilder.withConfig(configuration);
                }

//                clientBuilder = clientBuilder.register(COOKIE_MANAGER_FILTER);
                clients.put(domain, clientBuilder.build());
            }
        }
        return clients.get(domain);
    }

    public JaxRSClientInvoker(String domain, SSLContext context, String tokenManagerKey) {
        super(domain, tokenManagerKey);
        client = getClient(domain, context, BUILDER_SPI_FACADE.getInstance().buildConfig());
    }

    private ScheduledExecutorService executorService = ExecutorsHelper.newSingleThreadScheduledExecutor();

    private int pollingState = 0; // -1:服务不支持；0未开始；1轮询中

    private void executePoll() {
        if (pollingState == -1) return;
        executorService.schedule(
                new Runnable() {

                    @Override
                    public void run() {
                        String url = domain + (domain.endsWith("/") ? "Concrete" : "/Concrete")
                                + "/polling/15";
                        try {
                            Response response = request(url, HttpMethod.GET, null);
                            if (response.getStatus() == 404) {
                                log.warn("polling service not found: {}", domain);
                                pollingState = -1;
                            } else if (response.getStatus() / 100 == 2) {
                                try {
                                    List<MessagePojo<Object>> messages = response.readEntity(new GenericType<List<MessagePojo<Object>>>() {
                                    });
                                    for (Message<Object> message : messages) {
                                        MessageSubscriber.next(message.getSubject(), JSONSerializerFactory.getInstance().toJson(message.getBody()));
                                    }
                                } finally {
                                    executePoll();
                                }
                            }
                        } catch (Throwable e) {
                            log.error(e.getLocalizedMessage(), e);
                        }

                    }
                }, 50, TimeUnit.MILLISECONDS
        );

    }

    private synchronized void polling() {
        if (pollingState == 0) {
            pollingState = 1;
            executePoll();
        }
    }

    @Override
    protected Object invoke(String url, Unit unit, Object toSubmit) throws Throwable {

        Response response = request(url, unit.getInvokeType(), toSubmit);
        // store token
        String tokenId = response.getHeaderString(Token.CONCRETE_TOKEN_ID_KEY);
        if (!Common.isBlank(tokenId)) {
            String tokenKey = Common.isBlank(tokenManagerKey) ? domain : tokenManagerKey;
            ClientCommon.setTokenId(tokenKey, tokenId);
//            // polling
//            polling();
        }
        getCookieManager(domain).store(response.getCookies().values());

        String body = response.readEntity(String.class);
        return processResult(response.getStatus(), body, unit,
                response.getHeaders().keySet().contains(HEADER_ERROR_OCCURRED), url);
    }



    private Response request(String url, String method, Object body) throws URISyntaxException {
        URI uri = new URI(url);
        Invocation.Builder builder = client.target(url).request();
        StringBuilder str = new StringBuilder();
        str.append("url: ").append(url).append("\n").append("method: ").append(method);
        Subjoin subjoin = getServiceContext() == null ? null : getServiceContext().getSubjoin();
        String tokenId = ClientCommon.getTokenId(Common.isBlank(tokenManagerKey) ? domain : tokenManagerKey);
//        if (subjoin != null || !Common.isBlank(tokenId)) {
//            str.append("\nheaders:");
//            if (subjoin != null) {
//                for (String key : subjoin.keySet()) {
//                    builder = builder.header(key, subjoin.get(key));
//                    str.append("\n\t").append(key).append(": ").append(subjoin.get(key));
//                }
//            }
//            if (!Common.isBlank(tokenId)) {
//                builder = builder.header(CONCRETE_TOKEN_ID_KEY, tokenId);
//                str.append("\n\t").append(CONCRETE_TOKEN_ID_KEY).append(": ").append(tokenId);
//            }
//        }
        builder = buildHeaders(builder, str, subjoin, tokenId);

        StringBuilder cookies = new StringBuilder();
        for (Cookie cookie : getCookieManager(domain).load(uri.getPath())) {
            builder = builder.cookie(cookie);
            cookies.append(String.format("\n\tdomain: %s; name: %s; value: %s; path: %s, version: %d",
                    cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getVersion()));
        }
        if (cookies.length() > 0) {
            str.append("\ncookies:").append(cookies.toString());
        }

        if (body != null) {
            str.append("\ncontent:\n").append(getJSONSerializer().toJson(body));
        }

        log.debug("requestInfo: \n{}", str.toString());
        return body == null ?
                builder.build(method).invoke() :
                builder.build(method, Entity.entity(body,
                        MediaType.APPLICATION_JSON_TYPE.withCharset(getEncodingCharset()))).invoke();
    }
}
