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

    private static final ClosureContext<ServiceContext> CONTEXT = new StackClosureContext<ServiceContext>();

    public static final ServiceContext getServiceContext(){
        return CONTEXT.get();
    }

//    @Deprecated
//    public static final ClosureContext<String> MODEL = new StackClosureContext<String>();
//
//    @Deprecated
//    public static final ClosureContext<Integer> SIDE = new StackClosureContext<Integer>();
//
//    @Deprecated
//    public static final ClosureContext<Subjoin> SUBJOIN = new StackClosureContext<Subjoin>();
//
//    @Deprecated
//    public static final ClosureContext<Locale> LOCALE = new StackClosureContext<Locale>();
//
//    @Deprecated
//    public static final ClosureContext<Token> TOKEN = new StackClosureContext<Token>();
//
//    @Deprecated
//    public static final ClosureContext<Map<String, Object>> LOGGING = new StackClosureContext<Map<String, Object>>();
//
//    @Deprecated
//    public static final ClosureContext<AbstractUnit> CURRENT_UNIT = new StackClosureContext<AbstractUnit>();

    /**
     * 放入记录日志所需的数据
     *
     * @param key
     * @param value
     */
    public static final void putLoggingData(String key, Object value) {
//        LOGGING.get().put(key, value);
        getLogging().put(key, value);
    }

    public static final Map<String, Object> getLoggingData() {
//        return LOGGING.get();
        return getLogging();
    }

    private static final Map<String, Object> getLogging() {
        return CONTEXT.get().getLogging();
    }

    public static final Object runWithContext(final ServiceContext context, final ConcreteClosure runnable) {
        return new ConcreteClosure() {
            @Override
            public Object concreteRun() throws Throwable {
                return CONTEXT.run(context, runnable);
            }
        }.run();
    }

//    @Deprecated
//    public static final <T> ConcreteClosure run(final ClosureContext<T> closureContext, final T var, final ConcreteClosure runnable) {
//        return new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                return closureContext.run(var, runnable);
//            }
//        };
//    }

//    @Deprecated
//    public static final Object runWith(/*AbstractUnit unit, */String model, Subjoin subjoin, Token token, ConcreteClosure runnable) {
//        return
////                run(CURRENT_UNIT, unit,
//                run(MODEL, model,
//                        run(SIDE, SIDE_SERVER,
//                                run(SUBJOIN, subjoin,
//                                        run(LOCALE, getLocale(),
////                                        run(LOGGING, new HashMap<String, Object>(),
//                                                run(TOKEN, token, runnable)
////                                )
//                                        )
//                                )
//                        )
////                        )
//                ).run();
//    }

    private static Locale getLocale() {
        return CONTEXT.get().getSubjoin() == null ? Locale.getDefault() : CONTEXT.get().getSubjoin().getLocale();
//        return SUBJOIN.get() == null ? Locale.getDefault() : SUBJOIN.get().getLocale();
    }
}
