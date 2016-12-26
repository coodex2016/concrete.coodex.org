package cc.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 服务执行优先级，需要异步环境支持，例如Servlet 3.x, JAX-RS 2.x
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Priority {

    /**
     * 指定优先级组名
     *
     * @return
     */
    int value() default Thread.NORM_PRIORITY;
}
