package cc.coodex.concrete.jaxrs;

import cc.coodex.concrete.api.ConcreteService;
import cc.coodex.concrete.api.Priority;
import cc.coodex.concrete.common.*;
import cc.coodex.concrete.core.token.TokenManager;
import cc.coodex.concrete.core.token.TokenWrapper;
import cc.coodex.pojomocker.POJOMocker;
import cc.coodex.util.Common;
import cc.coodex.util.TypeHelper;
import com.alibaba.fastjson.JSON;

import javax.ws.rs.core.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cc.coodex.concrete.jaxrs.ClassGenerator.FRONTEND_DEV_MODE;

/**
 * 默认的JaxRS Resource，提供数据模拟功能
 * <p>
 * Created by davidoff shen on 2016-11-01.
 */
public abstract class AbstractJAXRSResource<T extends ConcreteService> {

    public static final String TOKEN_ID_IN_COOKIE = "CONCRETE_JAXRS_TOKENID";

    private final Class<T> clz = getInterfaceClass();

    @Context
    protected UriInfo uriInfo;


    @SuppressWarnings("unchecked")
    private <R> R convert(R result) {
        if (result == null) return null;
        if (result instanceof String && ConcreteHelper.getProfile().getBool("service.result.quoteSingleStr", true)) {
            return (R) JSON.toJSONString(result);
        }
        return result;
    }

    protected int getPriority(Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        if (priority == null) {
            priority = getInterfaceClass().getAnnotation(Priority.class);
        }
        return priority == null ?
                Thread.NORM_PRIORITY :
                Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, priority.value()));
    }

    private Token getToken(String tokenId, boolean force) {
        return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(tokenId, force);
    }

    @SuppressWarnings("unchecked")
    private Class<T> getInterfaceClass() {
        return (Class<T>) TypeHelper.findActualClassFrom(AbstractJAXRSResource.class.getTypeParameters()[0], getClass());
    }

    protected T getInstance() {
        return BeanProviderFacade.getBeanProvider().getBean(clz);
    }

    private static String getMethodNameInStack(int deep) {
        // getStackTrace +1, getMethodInStack +2
        return Thread.currentThread().getStackTrace()[deep + 2].getMethodName();
    }

    protected final Object mockResult() {
        return __mockResult();
    }

    private Object __mockResult() {
        try {
            Method method = findMethod(getMethodNameInStack(2), this.getClass());
            if (method.getReturnType() == void.class)
                return null;
            else
                return convert(POJOMocker.mock(method.getGenericReturnType(), getInterfaceClass()));
        } catch (Throwable th) {
            th.printStackTrace();
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage());
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
                        Class<?>[] parameterTypes = getParameterTypes(method.getParameterTypes());
                        try {
                            found = clz.getMethod(methodName, parameterTypes);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
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
        if (FRONTEND_DEV_MODE) {
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
        Object result = convert(TokenWrapper.closure(token, new ConcreteClosure() {
            public Object concreteRun() throws Throwable {
                Object instance = BeanProviderFacade.getBeanProvider().getBean(getInterfaceClass());
                if (paramCount == 0)
                    return method.invoke(instance);
                else
                    return method.invoke(instance, params);

            }
        }));

        Response.ResponseBuilder builder = result == null ? Response.noContent() : Response.ok();

        if (newToken) {
            URI root = uriInfo.getBaseUri();
            String cookiePath = ConcreteHelper.getProfile().getString("jaxrs.token.cookie.path");
            builder = builder.cookie(new NewCookie(new Cookie(
                    TOKEN_ID_IN_COOKIE, token.getTokenId(),
                    Common.isBlank(cookiePath) ? root.getPath() : cookiePath, null)));
        }

        if (result != null) builder = builder.entity(result);

        return builder.build();
    }


}
