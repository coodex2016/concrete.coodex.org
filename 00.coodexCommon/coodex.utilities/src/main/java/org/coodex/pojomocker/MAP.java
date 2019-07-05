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

package org.coodex.pojomocker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by davidoff shen on 2017-05-16.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RUNTIME)
public @interface MAP {

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @interface Key{
        int size() default 5;
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @interface Value{}

    @SuppressWarnings("rawtypes")
    @Deprecated
    Class keyType() default String.class;
    @Deprecated
    String keySeq() default "";
    @Deprecated
    Sequence.NotFound notFound() default Sequence.NotFound.WARN;

    @SuppressWarnings("rawtypes")
    @Deprecated
    Class keyMocker() default Mock.class;

    @SuppressWarnings("rawtypes")
    @Deprecated
    Class valueType() default Object.class;

    @SuppressWarnings("rawtypes")
    @Deprecated
    Class valueMocker() default Mock.class;

    @Deprecated
    int size() default 5;

}
