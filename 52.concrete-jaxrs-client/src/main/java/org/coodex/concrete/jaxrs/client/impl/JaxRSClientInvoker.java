/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.jaxrs.client.AbstractRemoteInvoker;
import org.coodex.concrete.jaxrs.client.JaxRSClientConfigBuilder;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;
import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;

/**
 * Created by davidoff shen on 2017-04-15.
 */
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

    public JaxRSClientInvoker(String domain, SSLContext context) {
        super(domain);
        client = getClient(domain, context, BUILDER_SPI_FACADE.getInstance().buildConfig());
    }

    @Override
    protected Object invoke(String url, Unit unit, Object toSubmit) throws Throwable {
        Response response = request(url, unit.getInvokeType(), toSubmit);
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
        Subjoin subjoin = getServiceContext().getSubjoin();
        if(subjoin != null){
            str.append("\nheaders:");
            for(String key : subjoin.keySet()){
                builder = builder.header(key, subjoin.get(key));
                str.append("\n\t").append(key).append(": ").append(subjoin.get(key));
            }
        }
        StringBuilder cookies = new StringBuilder();
        for (Cookie cookie : getCookieManager(domain).load(uri.getPath())) {
            builder = builder.cookie(cookie);
            cookies.append(String.format("\n\tdomain: %s; name: %s; value: %s; path: %s, version: %d",
                    cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getVersion()));
        }
        if(cookies.length() > 0){
            str.append("\ncookies:").append(cookies.toString());
        }

        if(body != null){
            str.append("\ncontent:\n").append(getJSONSerializer().toJson(body));
        }

        log.debug("requestInfo: \n{}", str.toString());
        return body == null ?
                builder.build(method).invoke() :
                builder.build(method, Entity.entity(body,
                        MediaType.APPLICATION_JSON_TYPE.withCharset(getEncodingCharset()))).invoke();
    }
}
