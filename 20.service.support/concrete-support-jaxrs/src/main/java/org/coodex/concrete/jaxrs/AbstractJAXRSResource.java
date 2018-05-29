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

package org.coodex.concrete.jaxrs;

import org.coodex.closure.CallableClosure;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.*;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.KEY_TOKEN;
import static org.coodex.concrete.common.ConcreteContext.runWithContext;
import static org.coodex.concrete.jaxrs.JaxRSHelper.KEY_CLIENT_PROVIDER;
import static org.coodex.util.TypeHelper.solve;
import static org.coodex.util.TypeHelper.typeToClass;

/**
 * 默认的JaxRS Resource，提供数据模拟功能
 * <p>
 * Created by davidoff shen on 2016-11-01.
 */
public abstract class AbstractJAXRSResource<T extends ConcreteService> {


//    public static final String TOKEN_ID_IN_COOKIE = CONCRETE_TOKEN_ID_KEY;

    private final Class<T> clz = getInterfaceClass();
    private final Map<String, Method> methodMap = new HashMap<String, Method>();
    @Context
    protected UriInfo uriInfo;
    @Context
    protected HttpHeaders httpHeaders;
    @Context
    protected HttpServletRequest httpRequest;

    private static boolean isDevModel() {
        return ConcreteHelper.isDevModel("jaxrs");
    }

    private static String getMethodNameInStack(int deep) {
        // getStackTrace +1, getMethodInStack +2
        return Thread.currentThread().getStackTrace()[deep + 2].getMethodName();
    }

    @SuppressWarnings("unchecked")
    private <R> R convert(R result) {
        return result;
    }

    protected int getPriority(Method method) {
        return ConcreteHelper.getPriority(method, clz);
    }

    //    private Token getToken(String tokenId, boolean force) {
//        return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(tokenId, force);
//    }
//    private Token getToken(String tokenId) {
//        return tokenId == null ? null :
//                BeanProviderFacade.getBeanProvider().getBean(TokenManager.class)
//                        .getToken(tokenId);
//    }

    @SuppressWarnings("unchecked")
    protected Class<T> getInterfaceClass() {
        return (Class<T>) typeToClass(solve(AbstractJAXRSResource.class.getTypeParameters()[0], getClass()));
    }

    protected Method findMethod(String methodName, Class<?> c) {
        String methodKey = getMethodNameInStack(3);
        synchronized (methodMap) {
            Method found = methodMap.get(methodKey);
            if (found == null) {
                Class<?> clz = c == null ? getInterfaceClass() : c;
                for (Method method : this.getClass().getMethods()) {
                    if (method.getName().equals(methodKey) && method.getAnnotation(CreatedByConcrete.class) != null) {
                        found = findActualMethod(methodName, clz, method.getAnnotation(CreatedByConcrete.class));
                        break;
                    }
                }
                if (found == null)
                    throw new NullPointerException("no impl found for: " + clz.getName() + "." + methodName);
                methodMap.put(methodKey, found);
            }
            return found;
        }
    }

    private int getParameterCount(Method method) {
        return method.getParameterTypes() == null ? 0 : method.getParameterTypes().length;
    }

    private Method findActualMethod(String methodName, Class<?> clz, CreatedByConcrete concrete) {
//        Class<?>[] parameterTypes = method.getDeclaredAnnotation(CreatedByConcrete.class).paramClasses();
        // 不确定原因，javassist int[]生成的注解不受支持
        //getParameterTypes(method.getParameterTypes());

        for (Method m : clz.getMethods()) {
            if (m.getName().equals(methodName) && getParameterCount(m) == concrete.paramCount()) {
                return m;
            }
        }
        throw new RuntimeException("no such method: " + methodName + ", paramCount: " + concrete.paramCount());
    }

    /**
     * 实际需要传递的参数从哪开始
     *
     * @return 按照生成Resource方法参数顺序，从1开始
     */
    protected abstract int getMethodStartIndex();

    /**
     * 根据动态创建method的参数类型反查接口method的参数类型
     *
     * @param parameterTypes 动态创建method的参数类型
     * @return 接口定义method的参数类型
     */
    private Class<?>[] getParameterTypes(Class<?>[] parameterTypes) {
        if (isDevModel()) {
            return parameterTypes;
        } else {
            int count = parameterTypes.length - getMethodStartIndex();
            Class<?>[] result = new Class[count];
            if (count > 0)
                System.arraycopy(parameterTypes, getMethodStartIndex(), result, 0, count);
            return result;
        }
    }

    protected ServiceContext buildContext(String tokenId/*,Token token, AbstractUnit unit*/) {

        return new JAXRSServiceContext(new Caller() {
            @Override
            public String getAddress() {
                String xff = httpHeaders.getHeaderString("X-Forwarded-For");
                if (xff != null) {
                    return xff.split(",")[0].trim();
                }
                return httpRequest.getRemoteAddr();
            }


            @Override
            public String getClientProvider() {
                return String.format("%s; %s",
                        httpHeaders.getHeaderString(KEY_CLIENT_PROVIDER),
                        httpHeaders.getHeaderString(HttpHeaders.USER_AGENT));
            }
        }, getSubjoin(), getRequestLocal(), tokenId);
    }

    private Locale getRequestLocal() {
        List<Locale> locales = httpHeaders.getAcceptableLanguages();
        return locales == null || locales.size() == 0 ?
                null : locales.get(0);
    }

    protected Response buildResponse(String tokenId, final Method method, final Object[] params, /*RunWithToken runWithToken*/ CallableClosure callable) {
//        final int paramCount = params == null ? 0 : params.length;
//        boolean newToken = false;
//        Token token = Common.isBlank(tokenId) ? null : getToken(tokenId, false);
//        if (token == null || !token.isValid()) {
//            token = getToken(Common.getUUIDStr(), true);
//            newToken = true;
//        }
//        Token token

//        Object result = runWithToken.runWithToken(getToken(tokenId));
        ServiceContext serviceContext = buildContext(tokenId);
        Object result = runWithContext(serviceContext, callable);

        Response.ResponseBuilder builder = result == null ? Response.noContent() : Response.ok();
        String tokenIdAfterInvoke = serviceContext.getTokenId();
        if (!Common.isSameStr(tokenId, tokenIdAfterInvoke) && !Common.isBlank(tokenIdAfterInvoke)) {
            builder = setTokenInfo(tokenIdAfterInvoke, builder);
        }

        if (result != null) {
            builder = builder.entity(result);
            if (result instanceof String) {
                builder = textType(builder);
            } else {
                builder = jsonType(builder);
            }
        }

        return builder.build();
    }

    protected Response invokeByTokenId(final String tokenId, final Method method, final Object[] params) {
        final int paramCount = params == null ? 0 : params.length;

        return buildResponse(tokenId, method, params,
                new CallableClosure() {

                    @Override
                    public Object call() throws Throwable {
                        if (!Polling.class.equals(method.getDeclaringClass()) && isDevModel()) {
                            return void.class.equals(method.getGenericReturnType()) ? null :
                                    MockerFacade.mock(method, getInterfaceClass());
                        } else {
                            Object instance = BeanProviderFacade.getBeanProvider().getBean(getInterfaceClass());
                            if (paramCount == 0)
                                return method.invoke(instance);
                            else
                                return method.invoke(instance, params);
                        }

                    }
                });

//        return buildResponse(tokenId, method, params, new RunWithToken() {
//            @Override
//            public Object runWithToken(Token token) {
//                return convert(
//                        runWithContext(
//                                buildContext(
//                                        tokenId,
//                                        JaxRSHelper.getUnitFromContext(
//                                                ConcreteHelper.getContext(method, getInterfaceClass()))),
//
//                                new CallableClosure() {
//
//                                    public Object call() throws Throwable {
//                                        if (!Polling.class.equals(method.getDeclaringClass()) && isDevModel()) {
//                                            return void.class.equals(method.getGenericReturnType()) ? null :
//                                                    MockerFacade.mock(method, getInterfaceClass());
//                                        } else {
//                                            Object instance = BeanProviderFacade.getBeanProvider().getBean(getInterfaceClass());
//                                            if (paramCount == 0)
//                                                return method.invoke(instance);
//                                            else
//                                                return method.invoke(instance, params);
//                                        }
//
//                                    }
//                                }));
//            }
//        });

    }

    protected abstract Response.ResponseBuilder textType(Response.ResponseBuilder builder);

    protected abstract Response.ResponseBuilder jsonType(Response.ResponseBuilder builder);

    private Subjoin getSubjoin() {
        return new JaxRSSubjoin(httpHeaders);
    }

    protected Response.ResponseBuilder setTokenInfo(String tokenId, Response.ResponseBuilder builder) {
        return builder.header(KEY_TOKEN, tokenId);
    }

    public interface RunWithToken {
        Object runWithToken(Token token);
    }

    public class ResponseBuilder {
        private final String tokenId;
        private final Method method;
        private final Object[] params;

        public ResponseBuilder(String tokenId, Method method, Object[] params) {
            this.tokenId = tokenId;
            this.method = method;
            this.params = params;
        }

        public Response build(/*RunWithToken runWithToken*/ CallableClosure callable) {
            return buildResponse(tokenId, method, params, callable);
        }

        public String getTokenId() {
            return tokenId;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getParams() {
            return params;
        }
    }


}
