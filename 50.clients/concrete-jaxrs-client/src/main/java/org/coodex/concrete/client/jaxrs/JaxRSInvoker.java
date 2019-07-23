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
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.mock.Mocker;
import org.coodex.util.Common;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.coodex.concrete.ClientHelper.getJSONSerializer;
import static org.coodex.concrete.ClientHelper.getSSLContext;
import static org.coodex.concrete.common.ConcreteHelper.isDevModel;
import static org.coodex.concrete.common.Token.CONCRETE_TOKEN_ID_KEY;
import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;
import static org.coodex.concrete.jaxrs.JaxRSHelper.getUnitFromContext;
import static org.coodex.util.GenericTypeHelper.toReference;

public class JaxRSInvoker extends AbstractSyncInvoker {

    private final static Logger log = LoggerFactory.getLogger(JaxRSInvoker.class);
//    private static final String DEFAULT_LOGGING_FEATURE_CLASS = "";

//    static {
//        // jersey logging feature
//        try {
//            Class<?> feature = Class.forName(getLoggingFeatureClassName());
//            clientBuilder = clientBuilder.register(feature);
//        } catch (Throwable th) {
//            log.info("register LoggingFeature failed. {}", th.getLocalizedMessage());
//        }
//    }

    private final Client client;

    JaxRSInvoker(JaxRSDestination destination) {
        super(destination);
        // TODO logging
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        client = destination.isSsl() ?
                clientBuilder.hostnameVerifier((s, sslSession) -> true).sslContext(getSSLContext(destination.getSsl())).build() :
                clientBuilder.build();
    }

//    private static String getLoggingFeatureClassName() {
//        // TODO
//        return DEFAULT_LOGGING_FEATURE_CLASS;
//    }

    private static Invocation.Builder buildHeaders(Invocation.Builder builder, StringBuilder str, Subjoin subjoin, String tokenId) {
        if (subjoin != null || !Common.isBlank(tokenId)) {
            str.append("\nheaders:");
            if (subjoin != null) {
                for (String key : subjoin.keySet()) {
                    builder = builder.header(key, subjoin.get(key));
                    str.append("\n\t").append(key).append(": ").append(subjoin.get(key));
                }
            }
            if (!Common.isBlank(tokenId)) {
                builder = builder.header(CONCRETE_TOKEN_ID_KEY, tokenId);
                str.append("\n\t").append(CONCRETE_TOKEN_ID_KEY).append(": ").append(tokenId);
            }
        }
        return builder;
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
            switch (pojoParams.length) {
                case 0:
                    break;
                case 1:
                    toSubmit = args[pojoParams[0].getIndex()];
                    break;
                default:
                    Map<String, Object> body = new HashMap<>();
                    for (JaxrsParam param : pojoParams) {
                        body.put(param.getName(), args[param.getIndex()]);
                    }
                    toSubmit = body;
            }
        }
        return toSubmit;
    }

    private String getEncodingCharset() {
        String charset = ((JaxRSDestination) getDestination()).getCharset();
        if (charset == null) charset = "utf-8";
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
        if (o == null) return null;
        if (TypeHelper.isPrimitive(o.getClass())) return o.toString();
        return getJSONSerializer().toJson(o);
    }

    @Override
    protected Object execute(Class clz, Method method, Object[] args) throws Throwable {
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
//                if (!Common.isBlank(tokenId) && !getDestination().isTokenTransfer()) {
                ClientTokenManagement.setTokenId(getDestination(), tokenId);
//                }

                String body = response.readEntity(String.class);

                if (log.isDebugEnabled()) {
                    builder = new StringBuilder();
                    builder.append("response\nstatus: ").append(response.getStatus()).append(";\nheaders:\n");
                    for (String key : response.getHeaders().keySet()) {
                        builder.append("\t").append(key).append(": ").append(response.getHeaderString(key)).append("\n");
                    }
                    builder.append("result:\n").append(body);
                    log.debug(builder.toString());
                }
                getContext().responseSubjoin(new JaxRSSubjoin(response.getHeaders()));
                return processResult(response.getStatus(), body, unit,
                        response.getHeaders().keySet().contains(HEADER_ERROR_OCCURRED), path);
            } catch (ClientException clientEx) {
                throw clientEx;
            } catch (Throwable th) {
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
//        URI uri = new URI(url);
        Invocation.Builder builder = client.target(url).request();
        StringBuilder str = new StringBuilder();
        str.append("url: ").append(url).append("\n").append("method: ").append(method);

        JaxRSClientContext context = getContext();
        Subjoin subjoin = context.getSubjoin();
        String tokenId = ClientTokenManagement.getTokenId(getDestination(), context.getTokenId());
//                getDestination().isTokenTransfer() ?
//                context.getTokenId() :
//                ClientTokenManagement.getTokenId(getDestination());

        builder = buildHeaders(builder, str, subjoin, tokenId);

        if (body != null && log.isDebugEnabled()) {
            str.append("\ncontent:\n").append(getJSONSerializer().toJson(body));
        }

        log.debug("requestInfo: \n{}", str.toString());
        return body == null ?
                builder.build(method).invoke() :
                builder.build(method, Entity.entity(body,
                        MediaType.APPLICATION_JSON_TYPE.withCharset(getEncodingCharset()))).invoke();
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new JaxRSClientContext(getDestination(), context);
    }

}
