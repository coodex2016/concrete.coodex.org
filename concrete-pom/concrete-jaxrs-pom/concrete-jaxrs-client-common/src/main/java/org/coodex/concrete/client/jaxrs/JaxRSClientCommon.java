/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ErrorInfo;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.jaxrs.JaxRSSubjoin;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.coodex.concrete.ClientHelper.getJSONSerializer;
import static org.coodex.concrete.common.ConcreteHelper.isPrimitive;
import static org.coodex.concrete.common.Subjoin.KEY_WARNINGS;
import static org.coodex.util.GenericTypeHelper.toReference;

public class JaxRSClientCommon {

    private static final Logger log = LoggerFactory.getLogger(JaxRSClientCommon.class);


    public static JaxRSClientException throwException(boolean errorOccurred, int code, String body, JaxrsUnit unit, String url) {
        if (errorOccurred) {
            ErrorInfo errorInfo = getJSONSerializer().parse(body, ErrorInfo.class);
            return new JaxRSClientException(errorInfo.getCode(), errorInfo.getMsg(), url, unit.getInvokeType());
        } else {
            return new JaxRSClientException(code, body, url, unit.getInvokeType());
        }
    }

    public static Object processResult(int code, String body, JaxrsUnit unit, boolean errorOccurred, String url) {
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

    public static Object getSubmitObject(JaxrsUnit unit, Object[] args) {
        Object toSubmit = null;
        JaxrsParam[] pojoParams = unit.getPojo();
        if (args != null) {
            if (pojoParams.length != 0) {//                case 1:
//                    toSubmit = args[pojoParams[0].getIndex()];
//                    break;
//            } else {
                if (pojoParams.length == 1 && !pojoParams[0].isAssembled()) {
                    return args[pojoParams[0].getIndex()];
                }
                Map<String, Object> body = new HashMap<>();
                for (JaxrsParam param : pojoParams) {
                    body.put(param.getName(), args[param.getIndex()]);
                }
                toSubmit = body;
            }
        }
        return toSubmit;
    }

    public static String getEncodingCharset(JaxRSDestination destination) {
        String charset = destination.getCharset();
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

    public static String encode(String s, String charset) throws UnsupportedEncodingException {
        if (s == null) return "";
        charset = Common.isBlank(charset) ? "utf-8" : charset;
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

    public static String toStr(Object o) {
        if (o == null) {
            return null;
        }
        if (isPrimitive(o.getClass())) {
            return o.toString();
        }
        return getJSONSerializer().toJson(o);
    }

    public static String getPath(JaxrsUnit unit, Object[] args, JaxRSDestination destination) throws UnsupportedEncodingException {
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
            builder.append(encode(node, getEncodingCharset(destination)));
        }
        return destination.getLocation() + builder;
    }

    public static JaxRSClientContext getContext() {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        if (serviceContext instanceof JaxRSClientContext) {
            return (JaxRSClientContext) serviceContext;
        } else {
            throw new RuntimeException(serviceContext + " is NOT JaxRSClientContext");
        }
    }

    public static void handleResponseHeaders(MultivaluedMap<String, ? extends Object> multivaluedMap) throws UnsupportedEncodingException {
        JaxRSSubjoin subjoin = new JaxRSSubjoin(multivaluedMap);
        String warnings = subjoin.get(KEY_WARNINGS);
        if (!Common.isBlank(warnings)) {
            subjoin.set(KEY_WARNINGS, Collections.singletonList(URLDecoder.decode(warnings, "UTF-8")));
        }
        getContext().responseSubjoin(subjoin);
    }
}
