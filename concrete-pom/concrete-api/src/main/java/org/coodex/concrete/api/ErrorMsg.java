/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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


import org.coodex.concrete.common.MessageFormatter;

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

    String value() default "";

    Class<? extends MessageFormatter> formatterClass() default MessageFormatter.class;

//    /**
//     * @deprecated (统一使用I18NFacade)
//     * @return
//     */
//    @Deprecated
//    Class<? extends MessagePatternLoader> patternLoaderClass() default MessagePatternLoader.class;

}
