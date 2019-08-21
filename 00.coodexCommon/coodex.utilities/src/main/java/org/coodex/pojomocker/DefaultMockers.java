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

import org.coodex.pojomocker.annotations.*;

import java.lang.annotation.*;

import static org.coodex.pojomocker.MockerFacade.MOCKER_LOADER;

/**
 * Created by davidoff shen on 2017-05-14.
 */
@DefaultMockers.DefaultAnnotations
@Deprecated
public class DefaultMockers implements Mocker<Annotation> {

    private static final DefaultAnnotations DEFAULT_ANNOTATIONS = DefaultMockers.class.getAnnotation(DefaultAnnotations.class);

    @Override
    public boolean accept(Annotation param) {
        return true;
    }

    @Override
    public Object mock(Annotation mockAnnotation, Class clazz) {

        Annotation annotation;

        if (byte.class == clazz || Byte.class.equals(clazz)) {
            annotation = DEFAULT_ANNOTATIONS.byteM();
        } else if (char.class == clazz || Character.class.equals(clazz)) {
            annotation = DEFAULT_ANNOTATIONS.charM();
        } else if (short.class == clazz || Short.class.equals(clazz)) {
            annotation = DEFAULT_ANNOTATIONS.shortM();
        } else if (int.class == clazz || Integer.class.equals(clazz)) {
            annotation = DEFAULT_ANNOTATIONS.intM();
        } else if (long.class == clazz || Long.class.equals(clazz)) {
            annotation = DEFAULT_ANNOTATIONS.longM();
        } else if (float.class == clazz || Float.class.equals(clazz)) {
            annotation = (DEFAULT_ANNOTATIONS.floatM());
        } else if (double.class == clazz || Double.class.equals(clazz)) {
            annotation = (DEFAULT_ANNOTATIONS.doubleM());
        } else if (boolean.class == clazz || Boolean.class.equals(clazz)) {
            annotation = (DEFAULT_ANNOTATIONS.booleanM());
        } else if (String.class.equals(clazz)) {
            annotation = (DEFAULT_ANNOTATIONS.stringM());
        } else {
            return null;
        }
        return MOCKER_LOADER.select(annotation).mock(annotation, clazz);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface DefaultAnnotations {
        INTEGER intM() default @INTEGER;

        BYTE byteM() default @BYTE;

        CHAR charM() default @CHAR;

        SHORT shortM() default @SHORT;

        LONG longM() default @LONG;

        STRING stringM() default @STRING;

        BOOLEAN booleanM() default @BOOLEAN;

        FLOAT floatM() default @FLOAT;

        DOUBLE doubleM() default @DOUBLE;
    }
}
