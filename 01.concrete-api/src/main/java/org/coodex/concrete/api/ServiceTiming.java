package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 指定服务提供时间，需要在非NoService上定义
 * 可指定多个服务时间验证类型，使用且规则。可通过serviceTiming.properties扩展
 * 默认为全天候
 * <p>
 * Created by davidoff shen on 2016-11-01.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceTiming {

    String[] value() default {""};
}
