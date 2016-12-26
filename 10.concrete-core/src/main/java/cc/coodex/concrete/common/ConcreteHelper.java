package cc.coodex.concrete.common;

import cc.coodex.concrete.api.Abstract;
import cc.coodex.concrete.api.ConcreteService;
import cc.coodex.concrete.api.MicroService;
import cc.coodex.util.Profile;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Stack;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class ConcreteHelper {

//    public static final String DEFAULT_TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID = "token.key.currentAccountId.default";

//    public static final String TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID =
//            getProfile().getString("token.key.currentAccountId", DEFAULT_TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID);

    public static Profile getProfile() {
        return ConcreteToolkit.getProfile();
    }

    public static String getServicesName(Class<?> clz) {
        return ConcreteToolkit.getServiceName(clz);
    }

    public static String getMethodName(Method method) {
        return ConcreteToolkit.getMethodName(method);
    }


    public static DefinitionContext getContextIfFound(Method method, Class<?> clz) {
        DefinitionContext context = getContext(method, clz);

        Assert.is(context == null, ErrorCodes.MODULE_DEFINITION_NOT_FOUND,
                method.getName(), clz.getCanonicalName());
        Assert.is(context.getDeclaringMethod() == null,
                ErrorCodes.UNIT_DEFINITION_NOT_FOUND,
                getServicesName(clz), method.getName());
        return context;
    }


    public static DefinitionContext getContext(Method method, Class<?> clz) {
        return getContext(method, clz, new Stack<Class<?>>());
    }

    @SuppressWarnings("unchecked")
    private static DefinitionContext getContext(Method method, Class<?> clz, Stack<Class<?>> stack) {

        if (clz == null) return null;

        // 如果找到根了，退出
        if (ConcreteService.class.equals(clz)
                || !ConcreteService.class.isAssignableFrom(clz))
            return null;

        // 如果在栈内则表示检查过了
        if (stack.contains(clz)) {
            return null;
        } else {
            stack.add(clz);
        }

        // 查找服务定义
        if (clz.getAnnotation(MicroService.class) != null &&
                clz.getAnnotation(Abstract.class) == null) {

            DefinitionContext context = new DefinitionContext();
            context.setDeclaringClass((Class<ConcreteService>) clz);

            // 查找方法
            Method unitMethod = findMethod(method, clz);
            if (unitMethod == null)
                return null;
            else {
                context.setDeclaringMethod(unitMethod);
                return context;
            }
        }

        for (Class<?> clazz : clz.getInterfaces()) {
            DefinitionContext context = getContext(method, clazz, stack);
            if (context != null) {
                return context;
            }
        }

        return getContext(method, clz.getSuperclass(), stack);
    }

    private static Method findMethod(Method method, Class<?> clz) {
        return findMethod(method, clz, new Stack<Class<?>>());
    }

    private static Method findMethod(Method method, Class<?> clz, Collection<Class<?>> stack) {
        if (stack.contains(clz))
            return null;
        else
            stack.add(clz);

        try {
            return clz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            for (Class<?> clazz : clz.getInterfaces()) {
                Method m = findMethod(method, clazz, stack);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public final static ConcreteException findException(Throwable th) {
        if (th == null) return null;

        Throwable t = th;
        while (t != null) {
            if (t instanceof ConcreteException)
                return (ConcreteException) t;
            t = t.getCause();
        }
        return null;
    }


    ///////////////////////////////////////////////////////
    public final static ConcreteException getException(Throwable th) {
        ConcreteException concreteException = findException(th);
        if (concreteException == null) {
            concreteException = new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage());
            concreteException.initCause(th);
        }
        return concreteException;
    }
}
