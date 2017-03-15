package org.coodex.concrete.common;

import org.coodex.concrete.api.ConcreteService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-01.
 */
public class DefinitionContext {

    private Class<? extends ConcreteService> declaringClass;
    private Method declaringMethod;

    DefinitionContext() {
    }

    void setDeclaringClass(Class<? extends ConcreteService> declaringClass) {
        this.declaringClass = declaringClass;
    }

    void setDeclaringMethod(Method declaringMethod) {
        this.declaringMethod = declaringMethod;
    }

    /**
     * BizModule定义的class
     *
     * @return
     */
    public Class<? extends ConcreteService> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * 定义了BizUnit的method
     *
     * @return
     */
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    public String getModuleName() {
        return ConcreteHelper.getServicesName(getDeclaringClass());
    }

    public String getMethodName() {
        return ConcreteHelper.getMethodName(getDeclaringMethod());
    }


    public <T extends Annotation> T getDeclaringAnnotation(Class<T> annotaionClass) {
        T annotation = declaringMethod == null ? null : declaringMethod.getAnnotation(annotaionClass);
        if (annotation == null) {
            return declaringClass.getAnnotation(annotaionClass);
        }
        return annotation;
    }

}
