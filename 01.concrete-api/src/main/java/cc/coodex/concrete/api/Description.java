package cc.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 业务描述注解，与ConcreteService一起使用
 * Created by davidoff shen on 2016-08-31.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
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
