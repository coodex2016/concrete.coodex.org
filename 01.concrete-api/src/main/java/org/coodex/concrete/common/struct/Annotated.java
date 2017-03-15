package org.coodex.concrete.common.struct;

import java.lang.annotation.Annotation;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public interface Annotated {
    /**
     * 获取某个注解
     *
     * @param annotationClass
     * @param <T>
     * @return
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * 获取全部注解
     *
     * @return
     */
    Annotation[] getAnnotations();
}
