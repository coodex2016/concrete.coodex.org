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

package org.coodex.concrete.common;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by davidoff shen on 2016-08-29.
 */
@SuppressWarnings("unused")
public class IF {

    /**
     * 当表达式exp为真时，抛出code异常
     *
     * @param exp     test
     * @param code    error code
     * @param objects error message params
     */
    public static void is(boolean exp, int code, Object... objects) {
//        is(exp, new ConcreteException(code, objects));
        if (exp) throw new ConcreteException(code, objects);
    }

    public static void is(Supplier<Boolean> exp, int code, Object... objects) {
        is(exp.get(), code, objects);
    }

    public static void is(boolean exp, ConcreteException ex) {
        if (exp) throw ex;
    }

    public static void is(Supplier<Boolean> exp, ConcreteException ex) {
        is(exp.get(), ex);
    }

    public static void is(boolean exp, String message) {
//        is(exp, ConcreteHelper.getException(new RuntimeException(message)));
        if (exp) throw ConcreteHelper.getException(new RuntimeException(message));
    }

    public static void is(Supplier<Boolean> exp, String message) {
        is(exp.get(), message);
    }

    /**
     * 表达式为否事，抛出code异常
     *
     * @param exp     exp
     * @param code    error code
     * @param objects error message params
     */
    public static void not(boolean exp, int code, Object... objects) throws ConcreteException {
        is(!exp, code, objects);
    }


    public static void not(boolean exp, ConcreteException ex) throws ConcreteException {
        is(!exp, ex);
    }

    public static void not(boolean exp, String message) throws ConcreteException {
        is(!exp, message);
    }

    public static void not(Supplier<Boolean> exp, int code, Object... objects) throws ConcreteException {
        not(exp.get(), code, objects);
    }

    public static void not(Supplier<Boolean> exp, ConcreteException ex) throws ConcreteException {
        not(exp.get(), ex);
    }

    public static void not(Supplier<Boolean> exp, String message) throws ConcreteException {
        not(exp.get(), message);
    }

    /**
     * 当对象o为null是，抛出code异常
     *
     * @param o       test object
     * @param code    error code
     * @param objects error message params
     * @return non null
     */
    public static <T> T isNull(T o, int code, Object... objects) throws ConcreteException {
        is(o == null, code, objects);
        return o;
//        return isNull(o, new ConcreteException(code, objects));
    }

    public static <T> T isNull(T o, ConcreteException exp) throws ConcreteException {
        is(o == null, exp);
        return o;
    }

    public static <T> T isNull(T o, String message) throws ConcreteException {
        is(o == null, message);
        return o;
//        return isNull(o, ConcreteHelper.getException(new RuntimeException(message)));
    }

    public static <T> T isNull(Supplier<T> o, int code, Object... objects) throws ConcreteException {
        return isNull(o.get(), code, objects);
//        return isNull(o, new ConcreteException(code, objects));
    }

    public static <T> T isNull(Supplier<T> o, ConcreteException exp) throws ConcreteException {
        return isNull(o.get(), exp);
    }

    public static <T> T isNull(Supplier<T> o, String message) throws ConcreteException {
        return isNull(o.get(), message);
//        return isNull(o, ConcreteHelper.getException(new RuntimeException(message)));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> T isNull(Optional<T> o, int code, Object... objects) throws ConcreteException {
        return o.orElseThrow(() -> new ConcreteException(code, objects));
//        return isNull(o, new ConcreteException(code, objects));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> T isNull(Optional<T> o, ConcreteException exp) throws ConcreteException {
        return o.orElseThrow(() -> exp);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> T isNull(Optional<T> o, String message) throws ConcreteException {
        return o.orElseThrow(() -> ConcreteHelper.getException(new RuntimeException(message)));
//        return isNull(o, ConcreteHelper.getException(new RuntimeException(message)));
    }


    /**
     * 当对象o不为null时，抛出code异常
     *
     * @param o       object
     * @param code    error code
     * @param objects error message params
     */
    public static void notNull(Object o, int code, Object... objects) throws ConcreteException {
//        notNull(o, new ConcreteException(code, objects));
        is((o instanceof Supplier ? ((Supplier<?>) o).get() : o) != null, code, objects);
    }

    public static void notNull(Object o, ConcreteException ex) throws ConcreteException {
        is((o instanceof Supplier ? ((Supplier<?>) o).get() : o) != null, ex);
    }

    public static void notNull(Object o, String message) throws ConcreteException {
        is((o instanceof Supplier ? ((Supplier<?>) o).get() : o) != null, message);
    }

}
