package cc.coodex.concrete.jaxrs.struct;

import cc.coodex.concrete.common.struct.AbstractParam;
import cc.coodex.util.ReflectHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static cc.coodex.concrete.jaxrs.JaxRSHelper.camelCase;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class Param extends AbstractParam {

    private final ReflectHelper.MethodParameter parameter;

    public Param(Method method, int index) {
        this.parameter = new ReflectHelper.MethodParameter(method, index);
    }

    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    @Override
    public Type getGenericType() {
        return parameter.getGenericType();
    }

    @Override
    public String getName() {
        return camelCase(parameter.getName());
    }

    @Override
    public int getIndex() {
        return parameter.getIndex();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }
}
