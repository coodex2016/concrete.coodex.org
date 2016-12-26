package cc.coodex.concrete.jaxrs;

import java.lang.annotation.*;

/**
 * Created by davidoff shen on 2016-11-25.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CreatedByConcrete {
}
