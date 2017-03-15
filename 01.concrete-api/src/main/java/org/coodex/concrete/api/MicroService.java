package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * Created by davidoff shen on 2016-08-31.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MicroService {

    String value() default "";

}
