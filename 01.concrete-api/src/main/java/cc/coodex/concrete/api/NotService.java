package cc.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 声明接口方法非服务，客户端应无法访问
 * Created by davidoff shen on 2016-11-01.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotService {
}
