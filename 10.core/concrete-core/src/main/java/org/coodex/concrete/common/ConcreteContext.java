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

import org.coodex.closure.CallableClosure;
import org.coodex.closure.ClosureContext;
import org.coodex.closure.StackClosureContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public final class ConcreteContext {

    public static final String KEY_TOKEN = Token.CONCRETE_TOKEN_ID_KEY;
    public static final String KEY_LOCALE = "CONCRETE-LOCALE";

    private static final ClosureContext<ServiceContext> CONTEXT = new StackClosureContext<ServiceContext>();

    private static final ClosureContext<Map<String, Object>> LOGGING = new StackClosureContext<Map<String, Object>>();
    private static Map<String, Object> emptyLogging = new Map<String, Object>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public Object put(String key, Object value) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<String> keySet() {
            return new HashSet<String>();
        }

        @Override
        public Collection<Object> values() {
            return new HashSet<Object>();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return new HashSet<Entry<String, Object>>();
        }
    };

    public static final ServiceContext getServiceContext() {
        return CONTEXT.get();
    }

    /**
     * 放入记录日志所需的数据
     *
     * @param key
     * @param value
     */
    public static final void putLoggingData(String key, Object value) {
        getLogging().put(key, value);
    }

    public static final Map<String, Object> getLoggingData() {
//        return LOGGING.get();
        return getLogging();
    }

    private static final Map<String, Object> getLogging() {
        Map<String, Object> logging = LOGGING.get();
        return logging == null ? emptyLogging : logging;
    }


//    @Deprecated
//    public static final Object runWithContext(final ServiceContext context, final ConcreteClosure runnable) {
//        return new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                return CONTEXT.run(context, runnable);
//            }
//        }.run();
//    }

    /**
     * TODO 需要考虑分离出去，Logging非必要模型
     *
     * @param callable
     * @return
     */
    public static final Object runWithLoggingContext(final CallableClosure callable) {
        try {
            return LOGGING.call(new ConcurrentHashMap<String, Object>(), callable);
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }


    public static final Object runWithContext(final ServiceContext context, final CallableClosure callable) {
        try {
            return CONTEXT.call(context, callable);
        } catch (Throwable throwable) {
            throw ConcreteHelper.getException(throwable);
        }
//        return new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                return CONTEXT.run(context, runnable);
//            }
//        }.run();
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

//    private static Locale getLocale() {
//        return CONTEXT.get().getSubjoin() == null ? Locale.getDefault() : CONTEXT.get().getSubjoin().getLocale();
////        return SUBJOIN.get() == null ? Locale.getDefault() : SUBJOIN.get().getLocale();
//    }
}
