package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 业务描述注解，与ConcreteService一起使用
 * <p>
 * 2017-02-21 可以装饰pojo属性和parameter。pojo属性指public field和getXXX isXXX(boolean)
 * Created by davidoff shen on 2016-08-31.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Description {

    /**
     * 描述对象供文档化的名称
     *
     * @return
     */
    String name();

    /**
     * 描述对象供文档化的详细说明<s>，如果以".MD"结尾，则使用markdown文件</s>
     *
     * @return
     */
    String description() default "";
}
