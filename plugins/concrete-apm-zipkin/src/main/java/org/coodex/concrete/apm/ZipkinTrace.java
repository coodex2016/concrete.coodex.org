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

package org.coodex.concrete.apm;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalSpan;
import brave.propagation.TraceContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.Singleton;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class ZipkinTrace extends AbstractTrace {

    private static final String TRACE_ID = "X-APM-TRACE-ID";
    private static final String SPAN_ID = "X-APM-SPAN-ID";
    private static Singleton<Tracing> tracingSingleton = new Singleton<Tracing>(
            new Singleton.Builder<Tracing>() {
                @Override
                public Tracing build() {
                    Tracing.Builder builder = Tracing.newBuilder();
                    String url = Config.get("zipkin.location",getAppSet());
                    if (url != null) {
                        Sender sender = OkHttpSender.create(url + "/api/v2/spans");
                        builder = builder.spanReporter(AsyncReporter.builder(sender)
                                .closeTimeout(500, TimeUnit.MILLISECONDS)
                                .build(SpanBytesEncoder.JSON_V2));
                    }


                    return builder.localServiceName(
                            Config.getValue("module.name", "concrete", getAppSet())
                    ).build();
                }
            }
    );
    private TraceContext traceContext;
    private String type;
    private Map<String, String> map = new ConcurrentHashMap<String, String>();
    private Span span = null;
    private CurrentTraceContext.Scope scope = null;

    public ZipkinTrace() {
        traceContext = CurrentTraceContext.Default.create().get();
    }

    public ZipkinTrace(Subjoin subjoin) {
        String strTraceId = subjoin.get(TRACE_ID);
        if (!Common.isBlank(strTraceId)) {
            TraceContext.Builder builder = TraceContext.newBuilder();

            long traceId = Common.toLong(strTraceId, 0l);
            long spanId = Common.toLong(subjoin.get(SPAN_ID), 0l);
            if (traceId == 0l || spanId == 0l) {
                traceContext = CurrentTraceContext.Default.create().get();
            } else {
                traceContext = builder.traceId(traceId).spanId(spanId).sampled(true).build();
            }
        }
    }


    private Tracer getTracer() {
        return tracingSingleton.get().tracer();
    }

    @Override
    protected void actualStart(String name) {
        if (traceContext == null) {
            span = getTracer().newTrace();
//            span = ThreadLocalSpan.create(getTracer()).next();
        } else {
            span = getTracer().newChild(traceContext);
//            span = getTracer().joinSpan(traceContext);
        }

        if (!Common.isBlank(name))
            span = span.name(name);
        if (!Common.isBlank(type))
            span = span.tag("type", type);



        if (map.size() > 0) {
            for (String key : map.keySet()) {
                span = span.tag(key, map.get(key));
            }
        }
        scope = CurrentTraceContext.Default.create().newScope(span.context());
        span = span.start();
//        ThreadLocalSpan.create(getTracer()).next();
    }

    @Override
    protected void actualFinish() {
        if (span != null) {
            span.finish();
            scope.close();
            ThreadLocalSpan.CURRENT_TRACER.remove();
            span = null;
        }
    }

    @Override
    public Trace type(String type) {
        this.type = type;
        return this;
    }

    @Override
    public Trace tag(String name, String value) {
        map.put(name, value);
        return this;
    }

//    @Override
//    public void appendTo(Trace trace) {
//        if (trace instanceof ZipkinTrace) {
//            traceContext = ((ZipkinTrace) trace).traceContext;
//        }
//    }

    @Override
    public void error(Throwable throwable) {
        if (span != null) {
            span.error(throwable);
        }
    }

    @Override
    public void hack(Subjoin subjoin) {
        if (span != null) {
            subjoin.set("X-APM-PROVIDER", Arrays.asList("zipkin"));
            if (span != null) {
                TraceContext traceContext = span.context();
                subjoin.set(TRACE_ID, Arrays.asList(String.valueOf(traceContext.traceId())));
                subjoin.set(SPAN_ID, Arrays.asList(String.valueOf(traceContext.spanId())));
            }
        }
    }
}
