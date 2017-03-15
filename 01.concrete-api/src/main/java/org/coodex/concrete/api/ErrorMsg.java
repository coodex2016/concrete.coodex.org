package org.coodex.concrete.api;


import org.coodex.concrete.common.MessageFormatter;
import org.coodex.concrete.common.MessagePatternLoader;

import java.lang.annotation.*;

/**
 * 用来标识每个异常编号的错误信息模版
 * Created by davidoff shen on 2016-09-01.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ErrorMsg {

    String value() default  "";

    Class<? extends MessageFormatter> formatterClass() default MessageFormatter.class;

    Class<? extends MessagePatternLoader> patternLoaderClass() default MessagePatternLoader.class;

}
