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

import org.coodex.concrete.common.Subjoin;
import org.coodex.concurrent.Parallel;
import org.coodex.util.LazyServiceLoader;

import java.util.concurrent.ExecutorService;

public class APM {

    private static final Trace doNothing = new Trace() {
        @Override
        public Trace start() {
            return this;
        }

        @Override
        public Trace start(String name) {
            return this;
        }

        @Override
        public Trace type(String type) {
            return this;
        }

        @Override
        public Trace tag(String name, String value) {
            return this;
        }


        @Override
        public void error(Throwable throwable) {

        }

        @Override
        public void finish() {

        }

        @Override
        public void hack(Subjoin subjoin) {

        }
    };
    private static final TraceFactory defaultFactory = new TraceFactory() {
        @Override
        public Trace create() {
            return doNothing;
        }

        @Override
        public Trace loadFrom(Subjoin subjoin) {
            return doNothing;
        }

        @Override
        public Parallel.RunnerWrapper createWrapper() {
            return null;
        }
    };
    private static final LazyServiceLoader<TraceFactory> traceFactoryServiceSingleton =
            new LazyServiceLoader<TraceFactory>(defaultFactory) {
            };

//            new Singleton<>(
//                    new Singleton.Builder<ServiceLoader<TraceFactory>>() {
//                        @Override
//                        public ServiceLoader<TraceFactory> build() {
//                            return new ServiceLoaderImpl<TraceFactory>() {
//
//                                private Trace doNothing = new Trace() {
//                                    @Override
//                                    public Trace start() {
//                                        return this;
//                                    }
//
//                                    @Override
//                                    public Trace start(String name) {
//                                        return this;
//                                    }
//
//                                    @Override
//                                    public Trace type(String type) {
//                                        return this;
//                                    }
//
//                                    @Override
//                                    public Trace tag(String name, String value) {
//                                        return this;
//                                    }
//
//
//                                    @Override
//                                    public void error(Throwable throwable) {
//
//                                    }
//
//                                    @Override
//                                    public void finish() {
//
//                                    }
//
//                                    @Override
//                                    public void hack(Subjoin subjoin) {
//
//                                    }
//                                };
//
//                                private TraceFactory defaultFactory = new TraceFactory() {
//                                    @Override
//                                    public Trace create() {
//                                        return doNothing;
//                                    }
//
//                                    @Override
//                                    public Trace loadFrom(Subjoin subjoin) {
//                                        return doNothing;
//                                    }
//
//                                    @Override
//                                    public Parallel.RunnerWrapper createWrapper() {
//                                        return null;
//                                    }
//                                };
//
//                                @Override
//                                public TraceFactory getDefault() {
//                                    return defaultFactory;
//                                }
//                            };
//                        }
//                    }
//            );

    public static Trace build() {
        return traceFactoryServiceSingleton.get().create();
    }

    public static Trace build(Subjoin subjoin) {
        return traceFactoryServiceSingleton.get().loadFrom(subjoin);
    }

    public static Parallel.Batch parallel(ExecutorService executorService, Runnable... runnables) {
//        return Parallel.builder().withExecutorService(executorService)

        return new Parallel(executorService,
                traceFactoryServiceSingleton.get().createWrapper()
        ).run(runnables);
    }

}
