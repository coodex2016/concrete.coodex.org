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

import org.coodex.closure.ClosureContext;
import org.coodex.closure.StackClosureContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public final class ConcreteContext {

    public static final int SIDE_SERVER = 0;
    public static final int SIDE_LOCAL_INVOKE = 1;
    public static final int SIDE_TEST = 2;
    public static final int SIDE_CLIENT = 3;

    public static final ClosureContext<Integer> SIDE = new StackClosureContext<Integer>();

    public static final ClosureContext<Subjoin> SUBJOIN = new StackClosureContext<Subjoin>();

    public static final ClosureContext<Locale> LOCALE = new StackClosureContext<Locale>();

    public static final ClosureContext<Token> TOKEN = new StackClosureContext<Token>();

    private static final ClosureContext<Map<String, Object>> LOGGING = new StackClosureContext<Map<String, Object>>();

    /**
     * 放入记录日志所需的数据
     *
     * @param key
     * @param value
     */
    public static final void putLoggingData(String key, Object value) {
        LOGGING.get().put(key, value);
    }

    public static final Map<String, Object> getLoggingData(){
        return LOGGING.get();
    }

    public static final <T> ConcreteClosure run(final ClosureContext<T> closureContext, final T var, final ConcreteClosure runnable) {
        return new ConcreteClosure() {
            @Override
            public Object concreteRun() throws Throwable {
                return closureContext.run(var, runnable);
            }
        };
    }

    public static final Object runWith(Subjoin subjoin, Token token, ConcreteClosure runnable) {
        return
                run(SIDE, SIDE_SERVER,
                        run(SUBJOIN, subjoin,
                                run(LOCALE, getLocale(),
                                        run(LOGGING, new HashMap<String, Object>(),
                                                run(TOKEN, token, runnable))))).run();
    }

    private static Locale getLocale() {
        return SUBJOIN.get() == null ? Locale.getDefault() : SUBJOIN.get().getLocale();
    }
}
