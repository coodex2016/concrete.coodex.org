package cc.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 服务领域
 * Created by davidoff shen on 2016-09-05.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Domain {

    /**
     * 定义了Domain后，说明该模块下单元所需权限为<I>Domain</I>.<I>role</I>
     *
     * @return
     */
    String value() default "";
}
