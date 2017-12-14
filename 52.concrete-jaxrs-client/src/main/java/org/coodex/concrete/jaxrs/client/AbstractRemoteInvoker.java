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

package org.coodex.concrete.jaxrs.client;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ErrorInfo;
import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.jaxrs.ClassGenerator;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.coodex.concrete.common.ConcreteHelper.isDevModel;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public abstract class AbstractRemoteInvoker extends AbstractInvoker {

    private final static Logger log = LoggerFactory.getLogger(AbstractRemoteInvoker.class);

//    private static final ServiceLoader<JSONSerializer> JSON_SERIALIZER_FACTORY = new ConcreteServiceLoader<JSONSerializer>() {
//
//        @Override
//        public JSONSerializer getConcreteDefaultProvider() {
//            return new FastJsonSerializer();
//        }
//    };

    public static JSONSerializer getJSONSerializer() {
        return JSONSerializerFactory.getInstance();
    }


    protected final String domain;
    protected final String tokenManagerKey;

    public AbstractRemoteInvoker(String domain, String tokenManagerKey) {
        this.domain = domain;
        this.tokenManagerKey = tokenManagerKey;
    }

    protected String getEncodingCharset() {
        String charset = ConcreteHelper.getProfile().getString("jaxrs.client.charset." + domain,
                ConcreteHelper.getProfile().getString("jaxrs.client.charset", "utf-8"));
        try {
            return Charset.forName(charset).displayName();
        } catch (UnsupportedCharsetException e) {
            log.warn("Unsupported charset: {}", charset);
            return "utf-8";
        }
    }

    @Override
    protected MethodInvocation getInvocation(final Unit unit, final Object[] args, final Object instance) {
        return new ClientMethodInvocation(instance, unit, args) {
            @Override
            public Object proceed() throws Throwable {
                if (isDevModel("jaxrs.client")) {
                    return MockerFacade.mock(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass());
                } else {

                    String path = domain;
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
                            for (int i = 0; i < unit.getParameters().length; i++) {
                                Param param = unit.getParameters()[i];

                                if (paramName.equals(param.getName())) {
                                    node = toStr(args[i]);
                                    break;
                                }
                            }
                        }
                        builder.append(URLEncoder.encode(node, getEncodingCharset()));
                    }
                    path = path + builder.toString();

                    // 找需要提交的对象
                    Object toSubmit = null;
                    Param[] pojoParams = unit.getPojo();
                    if (args != null) {
                        switch (pojoParams.length) {
                            case 0:
                                break;
                            case 1:
                                toSubmit = args[pojoParams[0].getIndex()];
                                break;
                            default:
                                Map<String, Object> body = new HashMap<String, Object>();
                                for (Param param : pojoParams) {
                                    body.put(param.getName(), args[param.getIndex()]);
                                }
                                toSubmit = body;
                        }
                    }
                    try {
                        return invoke(path, unit, toSubmit);
                    }catch (ClientException clientEx){
                        throw clientEx;
                    }catch (Throwable th){
                        ClientException ce = new ClientException(-1, th.getLocalizedMessage(), path, unit.getInvokeType());
                        ce.initCause(th);
                        throw ce;
                    }
                }
            }
        };
    }


    protected abstract Object invoke(String url, Unit unit, Object toSubmit) throws Throwable;

    protected String toStr(Object o) {
        if (o == null) return null;
        if (JaxRSHelper.isPrimitive(o.getClass())) return o.toString();
        return getJSONSerializer().toJson(o);
    }

    private ClientException throwException(boolean errorOccurred, int code, String body, Unit unit, String url) {
        if (errorOccurred) {
            ErrorInfo errorInfo = getJSONSerializer().parse(body, ErrorInfo.class);
            return new ClientException(errorInfo.getCode(), errorInfo.getMsg(), url, unit.getInvokeType());
        } else {
            return new ClientException(code, body, url, unit.getInvokeType());
        }
    }

    protected Object processResult(int code, String body, Unit unit, boolean errorOccurred, String url){

        log.debug("response\nstatus: {}; \nresult: \n{}", code, body);

        if (code >= 200 && code < 300) {
            return void.class.equals(unit.getReturnType()) ?
                    null :
                    getJSONSerializer().parse(body,
                            TypeHelper.toTypeReference(unit.getGenericReturnType(),
                                    unit.getDeclaringModule().getInterfaceClass()));
        } else {
            throw throwException(errorOccurred, code, body, unit, url);
        }
    }

//    protected abstract Object call(Module module, Unit unit, Object[] args) throws Throwable;
}
