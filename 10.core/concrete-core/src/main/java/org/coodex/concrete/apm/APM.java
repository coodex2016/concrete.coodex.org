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

import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.Subjoin;
import org.coodex.util.Singleton;

public class APM {

    private static Singleton<ConcreteServiceLoader<TraceFactory>> traceFactoryServiceSingleton =
            new Singleton<ConcreteServiceLoader<TraceFactory>>(
                    new Singleton.Builder<ConcreteServiceLoader<TraceFactory>>() {
                        @Override
                        public ConcreteServiceLoader<TraceFactory> build() {
                            return new ConcreteServiceLoader<TraceFactory>() {

                                private Trace doNothing = new Trace() {
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
                                private TraceFactory defaultFactory = new TraceFactory() {
                                    @Override
                                    public Trace create() {
                                        return doNothing;
                                    }

                                    @Override
                                    public Trace loadFrom(Subjoin subjoin) {
                                        return doNothing;
                                    }
                                };

                                @Override
                                protected TraceFactory getConcreteDefaultProvider() {
                                    return defaultFactory;
                                }
                            };
                        }
                    }
            );

    public static Trace build() {
        return traceFactoryServiceSingleton.getInstance().getInstance().create();
    }

    public static Trace build(Subjoin subjoin) {
        return traceFactoryServiceSingleton.getInstance().getInstance().loadFrom(subjoin);
    }

}
