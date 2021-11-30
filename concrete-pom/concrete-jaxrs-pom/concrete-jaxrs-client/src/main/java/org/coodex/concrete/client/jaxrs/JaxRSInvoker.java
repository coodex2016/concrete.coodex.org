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
import org.coodex.concrete.client.impl.AbstractSyncInvoker;
import org.coodex.concrete.common.*;
import org.coodex.concrete.jaxrs.JaxRSSubjoin;
import org.coodex.concrete.jaxrs.logging.ClientLogger;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.ClientHelper.getJSONSerializer;
import static org.coodex.concrete.ClientHelper.getSSLContext;
import static org.coodex.concrete.common.ConcreteHelper.isDevModel;
import static org.coodex.concrete.common.ConcreteHelper.isPrimitive;
import static org.coodex.concrete.common.Subjoin.KEY_WARNINGS;
import static org.coodex.concrete.common.Token.CONCRETE_TOKEN_ID_KEY;
import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;
import static org.coodex.concrete.jaxrs.JaxRSHelper.getUnitFromContext;
import static org.coodex.util.GenericTypeHelper.toReference;

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
        ClientBuilder clientBuilder = ClientBuilder.newBuilder().register(ClientLogger.class);
        Configuration configuration = CONFIGURATION_PROVIDER_SERVICE_LOADER.get().getConfiguration();
        if (configuration != null) {
            clientBuilder = clientBuilder.withConfig(configuration);
        }
        if (destination.getConnectTimeout() != null && destination.getConnectTimeout() > 0) {
            clientBuilder = clientBuilder.connectTimeout(destination.getConnectTimeout(), TimeUnit.MILLISECONDS);
        }
        client = destination.isSsl() ?
                clientBuilder.hostnameVerifier((s, sslSession) -> true).sslContext(getSSLContext(destination.getSsl())).build() :
                clientBuilder.build();
    }

    private static JaxRSClientException throwException(boolean errorOccurred, int code, String body, JaxrsUnit unit, String url) {
        if (errorOccurred) {
            ErrorInfo errorInfo = getJSONSerializer().parse(body, ErrorInfo.class);
            return new JaxRSClientException(errorInfo.getCode(), errorInfo.getMsg(), url, unit.getInvokeType());
        } else {
            return new JaxRSClientException(code, body, url, unit.getInvokeType());
        }
    }

    private static Object processResult(int code, String body, JaxrsUnit unit, boolean errorOccurred, String url) {


        if (code >= 200 && code < 300) {
            return (code == 204 || void.class.equals(unit.getReturnType())) ?
                    null :
                    getJSONSerializer().parse(body,
                            toReference(unit.getGenericReturnType(),
                                    unit.getDeclaringModule().getInterfaceClass()));
        } else {
            throw throwException(errorOccurred, code, body, unit, url);
        }
    }

    private static Object getSubmitObject(JaxrsUnit unit, Object[] args) {
        Object toSubmit = null;
        JaxrsParam[] pojoParams = unit.getPojo();
        if (args != null) {
            if (pojoParams.length != 0) {//                case 1:
//                    toSubmit = args[pojoParams[0].getIndex()];
//                    break;
//            } else {
                Map<String, Object> body = new HashMap<>();
                for (JaxrsParam param : pojoParams) {
                    body.put(param.getName(), args[param.getIndex()]);
                }
                toSubmit = body;
            }
        }
        return toSubmit;
    }

    private Invocation.Builder buildHeaders(Invocation.Builder builder/*, StringBuilder str*/, Subjoin subjoin, String tokenId) {
        JaxRSClientContext context = getContext();
        builder = builder.acceptLanguage(context.getLocale());
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

    private String getEncodingCharset() {
        String charset = ((JaxRSDestination) getDestination()).getCharset();
        if (charset == null) {
            charset = "utf-8";
        }
        try {
            return Charset.forName(charset).displayName();
        } catch (UnsupportedCharsetException e) {
            log.warn("Unsupported charset: {}， use \"UTF-8\"", charset);
            return "utf-8";
        }
    }

    private String encode(String s, String charset) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                builder.append("%20");
            } else {
                builder.append(URLEncoder.encode(String.valueOf(c), charset));
            }
        }
        return builder.toString();
    }

    private String toStr(Object o) {
        if (o == null) {
            return null;
        }
        if (isPrimitive(o.getClass())) {
            return o.toString();
        }
        return getJSONSerializer().toJson(o);
    }

    @Override
    protected Object execute(Class<?> clz, Method method, Object[] args) throws Throwable {
        JaxrsUnit unit = getUnitFromContext(ConcreteHelper.getContext(method, clz));
        if (isDevModel("jaxrs.client")) {
            return Mocker.mockMethod(
                    unit.getMethod(),
                    unit.getDeclaringModule().getInterfaceClass());
        } else {

            String path = getDestination().getLocation();
            //+ unit.getDeclaringModule().getName();
            StringTokenizer stringTokenizer = new StringTokenizer(
                    unit.getDeclaringModule().getName() + "/" + unit.getName(), "/");
            StringBuilder builder = new StringBuilder();

            while (stringTokenizer.hasMoreElements()) {
                String node = stringTokenizer.nextToken();

                if (Common.isBlank(node)) continue;
                builder.append("/");
                if (node.startsWith("{") && node.endsWith("}")) {
                    //参数
                    String paramName = new String(node.toCharArray(), 1, node.length() - 2);
                    JaxrsParam[] params = unit.getParameters();
                    for (int i = 0; i < params.length; i++) {
                        JaxrsParam param = params[i];

                        if (paramName.equals(param.getName())) {
                            node = toStr(args[i]);
                            break;
                        }
                    }
                }
                builder.append(encode(node, getEncodingCharset()));
            }
            path = path + builder.toString();

            // 找需要提交的对象
            Object toSubmit = getSubmitObject(unit, args);

            try {
                Response response = request(path, unit.getInvokeType(), toSubmit);

                String tokenId = response.getHeaderString(Token.CONCRETE_TOKEN_ID_KEY);
                ClientTokenManagement.setTokenId(getDestination(), tokenId);

                String body = response.readEntity(String.class);

                JaxRSSubjoin subjoin = new JaxRSSubjoin(response.getHeaders());
                String warnings = subjoin.get(KEY_WARNINGS);
                if (!Common.isBlank(warnings)) {
                    subjoin.set(KEY_WARNINGS, Collections.singletonList(URLDecoder.decode(warnings, "UTF-8")));
                }
                getContext().responseSubjoin(subjoin);
                return processResult(response.getStatus(), body, unit,
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

    private JaxRSClientContext getContext() {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        if (serviceContext instanceof JaxRSClientContext) {
            return (JaxRSClientContext) serviceContext;
        } else {
            throw new RuntimeException(serviceContext + " is NOT JaxRSClientContext");
        }
    }

    private Response request(String url, String method, Object body) {
        Invocation.Builder builder = getInvokerBuilder(url);

        return body == null ?
                builder.build(method).invoke() :
                builder.build(method, Entity.entity(body,
                        MediaType.APPLICATION_JSON_TYPE.withCharset(getEncodingCharset()))).invoke();
    }

    private Invocation.Builder getInvokerBuilder(String url) {
        Invocation.Builder builder = client.target(url).request();

        JaxRSClientContext context = getContext();
        Subjoin subjoin = context.getSubjoin();
        String tokenId = ClientTokenManagement.getTokenId(getDestination(), context.getTokenId());

        return buildHeaders(builder, subjoin, tokenId);
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new JaxRSClientContext(getDestination(), context);
    }

}
