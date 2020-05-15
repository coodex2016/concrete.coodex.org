/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 用来标识一个类是错误代码类，该类中，所有的public static final int类型的域均会作为错误信息进行注册
 * <p>
 * 获取一个code的模板时，如果指定了ErrorCode.Template.value()，则使用此模板，否则concrete会通过I18N进行搜索, 键为：
 * ErrorCode.value() + '.' + Key.value(); Key.value默认为code的值，ErrorCode.value的默认值为“message”
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Overlay
public @interface ErrorCode {

    String DEFAULT_NAMESPACE = "message";

    /**
     * @return 此异常类的命名空间，默认为message
     */
    String value() default DEFAULT_NAMESPACE;

    /**
     * 错误代码模板在I18N中的键
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Key {
        String value();
    }

    /**
     * 错误代码的模板
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Template {
        String value();
    }

}
