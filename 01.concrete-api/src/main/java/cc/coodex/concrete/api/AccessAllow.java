package cc.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 业务接口访问权限定义，与BizUnit一起使用
 * <p>
 * Created by davidoff shen on 2016-09-01.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessAllow {

    /**
     * 所有人都能访问
     */
    String EVERYBODY = "EVERYBODY";

    String PREROGATIVE = "*";

    /**
     * 访问业务所需的角色
     * 如置空，则表明只要是有效用户即可访问
     *
     * @return
     */
    String[] roles() default {EVERYBODY};
}
