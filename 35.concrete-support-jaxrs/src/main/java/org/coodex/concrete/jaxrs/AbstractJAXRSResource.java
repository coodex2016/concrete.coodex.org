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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;

import javax.ws.rs.core.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.*;
import static org.coodex.util.TypeHelper.solve;
import static org.coodex.util.TypeHelper.typeToClass;

/**
 * 默认的JaxRS Resource，提供数据模拟功能
 * <p>
 * Created by davidoff shen on 2016-11-01.
 */
public abstract class AbstractJAXRSResource<T extends ConcreteService> {

    public static final String TOKEN_ID_IN_COOKIE = "CONCRETE_JAXRS_TOKEN_ID";

    private final Class<T> clz = getInterfaceClass();

    @Context
    protected UriInfo uriInfo;

    @Context
    protected HttpHeaders httpHeaders;


    @SuppressWarnings("unchecked")
    private <R> R convert(R result) {
        if (result == null) return null;
        if (result instanceof String && ConcreteHelper.getProfile().getBool("service.result.quoteSingleStr", true)) {
            return (R) JSONSerializerFactory.getInstance().toJson(result);
        }
        return result;
    }

    protected int getPriority(Method method) {
        return ConcreteHelper.getPriority(method, clz);
//        DefinitionContext context = ConcreteHelper.getContext(method, clz);
//        Priority priority = context.getAnnotation(Priority.class);
////                method.getAnnotation(Priority.class);
////        if (priority == null) {
////            priority = getInterfaceClass().getAnnotation(Priority.class);
////        }
//        return priority == null ?
//                Thread.NORM_PRIORITY :
//                Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, priority.value()));
    }

    private Token getToken(String tokenId, boolean force) {
        return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(tokenId, force);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getInterfaceClass() {
        return (Class<T>) typeToClass(solve(AbstractJAXRSResource.class.getTypeParameters()[0], getClass()));
    }

    protected T getInstance() {
        return BeanProviderFacade.getBeanProvider().getBean(clz);
    }

    private static String getMethodNameInStack(int deep) {
        // getStackTrace +1, getMethodInStack +2
        return Thread.currentThread().getStackTrace()[deep + 2].getMethodName();
    }

    protected final Object mockResult(String methodName) {
        return __mockResult(methodName);
    }

    private Object __mockResult(String methodName) {
        try {
            Method method = findMethod(methodName, null);
            if (method.getReturnType() == void.class)
                return null;
            else
                return convert(MockerFacade.mock(method, getInterfaceClass()));
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }

    private final Map<String, Method> methodMap = new HashMap<String, Method>();


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
        if (ClassGenerator.FRONTEND_DEV_MODE) {
            return parameterTypes;
        } else {
            int count = parameterTypes.length - getMethodStartIndex();
            Class<?>[] result = new Class[count];
            if (count > 0)
                System.arraycopy(parameterTypes, getMethodStartIndex(), result, 0, count);
            return result;
        }
    }

    protected Response invokeByTokenId(String tokenId, final Method method, final Object[] params) {
        final int paramCount = params == null ? 0 : params.length;
        boolean newToken = false;
        Token token = Common.isBlank(tokenId) ? null : getToken(tokenId, false);
        if (token == null || !token.isValid()) {
            token = getToken(Common.getUUIDStr(), true);
            newToken = true;
        }


        Object result = convert(runWith(
                //JaxRSHelper.getUnitFromContext(ConcreteHelper.getContext(method, getInterfaceClass())),
                JaxRSHelper.JAXRS_MODEL, getSubjoin(), token,
                run(CURRENT_UNIT, JaxRSHelper.getUnitFromContext(ConcreteHelper.getContext(method, getInterfaceClass())),
                        new ConcreteClosure() {

                            public Object concreteRun() throws Throwable {
                                Object instance = BeanProviderFacade.getBeanProvider().getBean(getInterfaceClass());
                                if (paramCount == 0)
                                    return method.invoke(instance);
                                else
                                    return method.invoke(instance, params);

                            }
                        })));

        Response.ResponseBuilder builder = result == null ? Response.noContent() : Response.ok();

        if (newToken) {
            builder = setCookie(token, builder);
        }

        if (result != null) builder = builder.entity(result);

        return builder.build();
    }


    private Subjoin getSubjoin() {
        return new JaxRSSubjoin(httpHeaders);
    }


    protected Response.ResponseBuilder setCookie(Token token, Response.ResponseBuilder builder) {
        URI root = uriInfo.getBaseUri();
        String cookiePath = ConcreteHelper.getProfile().getString("jaxrs.token.cookie.path");
        builder = builder.cookie(new NewCookie(new Cookie(
                TOKEN_ID_IN_COOKIE, token.getTokenId(),
                Common.isBlank(cookiePath) ? root.getPath() : cookiePath, null)));
        return builder;
    }


}
