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

package org.coodex.util;

import org.coodex.closure.CallableClosure;
import org.coodex.closure.StackClosureContext;
import org.coodex.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Tracer {
    private final static Logger log = LoggerFactory.getLogger(Tracer.class);
    private static final StackClosureContext<Map<String, Object>> tracer_context = new StackClosureContext<Map<String, Object>>();
    private static Singleton<Boolean> TRACE_ENABLED = new Singleton<Boolean>(new Singleton.Builder<Boolean>() {
        @Override
        public Boolean build() {
//            return Common.toBool(System.getProperty("org.coodex.util.Tracer"), false);
            return Config.getValue("org.coodex.util.Tracer", false);
        }
    });
    private static String START_TIME_KEY = Common.getUUIDStr();
    private Logger logger = log;
    private Common.Supplier<String> nameSupplier = null;

    private Tracer() {
    }

    public static void putTrace(String key, Object value) {
        if (isEnabled() && tracer_context.get() != null)
            tracer_context.get().put(key, value);
    }

    public static void start(String label) {
        if (isEnabled() && tracer_context.get() != null) {
            getStartTimeMap().put(label, Clock.currentTimeMillis());
        }
    }

    private static Map<String, Long> getStartTimeMap() {
        //noinspection unchecked
        return (Map<String, Long>) tracer_context.get().get(START_TIME_KEY);
    }

    public static void end(String label) {
        if (isEnabled() && tracer_context.get() != null && getStartTimeMap().containsKey(label)) {
            long used = Clock.currentTimeMillis() - getStartTimeMap().get(label);
            getStartTimeMap().remove(label);
            putTrace(label,
                    "used " + used + " ms");
        }
    }

    public static Tracer newTracer() {
        return new Tracer();
    }

    private static boolean isEnabled() {
        return TRACE_ENABLED.get();
    }

    public Tracer logger(Logger logger) {
        if (logger == null) throw new NullPointerException("logger is null.");
        this.logger = logger;
        return this;
    }

    public Tracer logger(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
        return this;
    }

    public Tracer logger(Class<?> clz) {
        this.logger = LoggerFactory.getLogger(clz);
        return this;
    }

    public Tracer named(final String name) {
        nameSupplier = Common.isBlank(name) ? null : new Common.Supplier<String>() {
            @Override
            public String get() {
                return name;
            }
        };
        return this;
    }

    public Tracer named(Common.Supplier<String> nameSupplier) {
        this.nameSupplier = nameSupplier;
        return this;
    }

    public void trace(final Runnable runnable) {
        if (isEnabled()) {
            trace(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    runnable.run();
                    return null;
                }
            });
        } else {
            runnable.run();
        }
    }

    public <T> T trace(final Callable<T> callable) {
        if (isEnabled()) {
            long start = Clock.currentTimeMillis();
            Throwable throwable = null;
            Map<String, Object> context = new LinkedHashMap<String, Object>();
            try {
                context.put(START_TIME_KEY, new HashMap<String, Long>());
                //noinspection unchecked
                return (T) tracer_context.call(context, new CallableClosure() {
                    @Override
                    public Object call() throws Throwable {
                        return callable.call();
                    }
                });
            } catch (Throwable th) {
                throwable = th;
            } finally {
                long used = Clock.currentTimeMillis() - start;
                if (throwable != null && logger.isErrorEnabled()) {
                    String info = buildTraceInfo(context);
                    log.error(info + "used {} ms.", used, throwable);
                } else if (throwable == null && logger.isInfoEnabled()) {
                    String info = buildTraceInfo(context);
                    log.info(info + "used {} ms.", used);
                }
            }
            throw Common.runtimeException(throwable);
        } else {
            try {
                return callable.call();
            } catch (Exception e) {
                throw Common.runtimeException(e);
            }
        }
    }

    private String buildTraceInfo(Map<String, Object> context) {
        StringBuilder builder = new StringBuilder();

        if (nameSupplier != null)
            builder.append("TRACER ").append(nameSupplier.get()).append(": [");
        else
            builder.append("[");

        boolean first = true;
        for (Map.Entry<String, Object> entry : context.entrySet()) {

            if (START_TIME_KEY.equals(entry.getKey())) continue;
            if (!first)
                builder.append("; ");
            builder.append(entry.getKey()).append(": ")
                    .append(entry.getValue() == null ? null : entry.getValue().toString());
            first = false;
        }
        builder.append("] ");
        return builder.toString();
    }

}
