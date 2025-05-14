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


/*
 * Copyright 2013-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.coodex.concrete.apm.mysql8;

import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.log.Log;
import org.coodex.concrete.apm.Trace;

import java.util.Properties;

import static org.coodex.concrete.apm.mysql8.TracingQueryInterceptor.traceThreadLocal;

/**
 * A MySQL exception interceptor that will annotate spans with SQL error codes.
 *
 * <p>To use it, both TracingQueryInterceptor and TracingExceptionInterceptor must be added by
 * appending <code>?queryInterceptors=brave.mysql8.TracingQueryInterceptor&exceptionInterceptors=brave.mysql8.TracingExceptionInterceptor</code>.
 *
 *
 * <code>?queryInterceptors=org.coodex.concrete.apm.mysql8.TracingQueryInterceptor&exceptionInterceptors=org.coodex.concrete.apm.mysql8.TracingExceptionInterceptor</code>
 */
public class TracingExceptionInterceptor implements ExceptionInterceptor {

    @Override
    public ExceptionInterceptor init(Properties properties, Log log) {
        String queryInterceptors = properties.getProperty("queryInterceptors");
        if (queryInterceptors == null ||
                !queryInterceptors.contains(TracingQueryInterceptor.class.getName())) {
            throw new IllegalStateException(
                    "TracingQueryInterceptor must be enabled to use TracingExceptionInterceptor.");
        }
        return new TracingExceptionInterceptor();
    }

    @Override
    public void destroy() {
        // Don't care
    }

    @Override
    public Exception interceptException(Exception e) {
//        Span span = ThreadLocalSpan.CURRENT_TRACER.remove();
//        if (span == null || span.isNoop()) return null;
//
//        if (e instanceof SQLException) {
//            span.tag("error", Integer.toString(((SQLException) e).getErrorCode()));
//        }
//
//        span.finish();
//
//        return null;
        Trace trace = traceThreadLocal.get();
        traceThreadLocal.remove();
        if (trace != null) {
            trace.error(e);
            trace.finish();
        }
        return null;
    }
}