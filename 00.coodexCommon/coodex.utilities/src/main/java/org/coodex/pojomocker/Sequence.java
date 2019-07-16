/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.pojomocker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD,ElementType.ANNOTATION_TYPE})
/**
 * 用定义模拟用的序列数据
 */
@Deprecated
public @interface Sequence {

    /**
     * @return 指定序列发生器名称，当该Mock作用域内的Item key与之一致时，则使用此序列发生器的值
     */
    String key();

    /**
     * todo 多级数组或者set怎么弄？
     *
     * @return
     */
    Class<? extends SequenceGenerator> sequenceType();

    enum NotFound {
        IGNORE, WARN, ERROR
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
            /**
             * 序列数据的条目
             */
    @interface Item {
        /**
         * @return 为空时，表示选择当前上下文中的序列发生器产生的值，否则，选择上下文中key值相同的序列发生器所产生的值，如果找不到，则报错
         */
        String key();

        NotFound notFound() default NotFound.WARN;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD,ElementType.ANNOTATION_TYPE})
            /**
             * 用在集合属性或者方法上，指示当前上下文使用哪个序列发生器
             */
    @interface Use {

        String key();

        NotFound notFound() default NotFound.WARN;
    }

}
