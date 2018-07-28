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

import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concurrent.Parallel;

public class ZipkinTraceFactory implements TraceFactory {
    @Override
    public Trace create() {
        return new ZipkinTrace();
    }

    @Override
    public Trace loadFrom(Subjoin subjoin) {
        return new ZipkinTrace(subjoin);
    }

    @Override
    public Parallel.RunnerWrapper createWrapper() {
        final TraceContext context = CurrentTraceContext.Default.create().get();
        return context == null ? null :
                new Parallel.RunnerWrapper() {
                    @Override
                    public Runnable wrap(final Runnable runnable) {
                        return new Runnable() {
                            @Override
                            public void run() {
                                CurrentTraceContext.Scope scope = CurrentTraceContext.Default.create().maybeScope(context);
                                try {
                                    runnable.run();
                                } finally {
                                    scope.close();
                                }
                            }
                        };
                    }
                };
    }
}
