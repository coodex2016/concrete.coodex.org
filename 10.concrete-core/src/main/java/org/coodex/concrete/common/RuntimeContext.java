package org.coodex.concrete.common;

import org.coodex.concrete.api.ConcreteService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-06.
 */
public class RuntimeContext extends DefinitionContext {

    private RuntimeContext() {
    }

    @SuppressWarnings("unchecked")
    public static final RuntimeContext getRuntimeContext(Method method, Class<?> clz) {
        DefinitionContext context = ConcreteHelper.getContext(method, clz);
        if (context == null) return null;
        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setDeclaringMethod(context.getDeclaringMethod());
        runtimeContext.setDeclaringClass(context.getDeclaringClass());
        runtimeContext.setActualClass((Class<? extends ConcreteService>) clz);
        runtimeContext.setActualMethod(method);
        return runtimeContext;
    }

    private Class<? extends ConcreteService> actualClass;

    private Method actualMethod;

    public Class<? extends ConcreteService> getActualClass() {
        return actualClass;
    }


    public Method getActualMethod() {
        return actualMethod;
    }

    void setActualClass(Class<? extends ConcreteService> actualClass) {
        this.actualClass = actualClass;
    }

    void setActualMethod(Method actualMethod) {
        this.actualMethod = actualMethod;
    }

    /**
     * 获取运行期定义的Annotaion
     *
     * @param annotationType
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (actualMethod == null) return null;

        Class clz = actualMethod.getDeclaringClass();
        while (clz != null) {
            try {
                Method method = clz.getMethod(actualMethod.getName(), actualMethod.getParameterTypes());
                T annotation = method.getAnnotation(annotationType);
                if (annotation != null) return annotation;
                clz = method.getDeclaringClass();
            } catch (NoSuchMethodException e) {
            }
            clz = clz.getSuperclass();
        }

        return getDeclaringMethod() == null ? null : getDeclaringMethod().getAnnotation(annotationType);
//        return getDeclaringAnnotation(annotationType);
    }
}
