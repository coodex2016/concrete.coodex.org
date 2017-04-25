/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

/**
 * Created by davidoff shen on 2016-08-29.
 */
public final class Assert {

    /**
     * 当表达式exp为真时，抛出code异常
     *
     * @param exp
     * @param code
     * @param objects
     */
    public static final void is(boolean exp, int code, Object... objects) {
        is(exp, new ConcreteException(code, objects));
    }

    public static final void is(boolean exp, ConcreteException ex) {
        if (exp) throw ex;
    }

    /**
     * 表达式为否事，抛出code异常
     *
     * @param exp
     * @param code
     * @param objects
     */
    public static final void not(boolean exp, int code, Object... objects) {
        not(exp, new ConcreteException(code, objects));
    }

    public static final void not(boolean exp, ConcreteException ex) {
        is(!exp, ex);
    }

    /**
     * 当对象o为null是，抛出code异常
     *
     * @param o
     * @param code
     * @param objects
     * @return
     */
    public static final <T> T isNull(T o, int code, Object... objects) {
        return isNull(o, new ConcreteException(code, objects));
    }

    public static final <T> T isNull(T o, ConcreteException exp) {
        is(o == null, exp);
        return o;
    }

    /**
     * 当对象o不为null时，抛出code异常
     *
     * @param o
     * @param code
     * @param objects
     */
    public static final void notNull(Object o, int code, Object... objects) {
        notNull(o, new ConcreteException(code, objects));
    }

    public static final void notNull(Object o, ConcreteException ex) {
        is(o != null, ex);
    }


}
