/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.junit.enhance;


import org.coodex.util.LazySelectableServiceLoader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.junit.enhance.MapContextUtil.appendContext;
import static org.coodex.junit.enhance.MapContextUtil.buildMapContext;
import static org.coodex.junit.enhance.TestUtils.CONTEXT;

public class CoodexEnhanceExtension implements InvocationInterceptor {
    private static final LazySelectableServiceLoader<ReflectiveInvocationContext<Method>, ContextProvider4JUnit5> CONTEXT_PROVIDER_LOADER =
            new LazySelectableServiceLoader<ReflectiveInvocationContext<Method>, ContextProvider4JUnit5>(
                    new ContextProvider4JUnit5() {
                        @Override
                        public Map<String, Object> createContext(ReflectiveInvocationContext<Method> method) {
                            return buildMapContext(new MapContextUtil.Annotated() {
                                @Override
                                public <A extends Annotation> A getAnnotation(Class<A> aClass) {
                                    return method.getExecutable().getAnnotation(aClass);
                                }
                            });
                        }

                        @Override
                        public boolean accept(ReflectiveInvocationContext<Method> param) {
                            return param.getExecutable().getAnnotation(MapContext.class) != null ||
                                    param.getExecutable().getAnnotation(Entry.class) != null;
                        }
                    }
            ) {
            };

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        if (CONTEXT.get() != null) {
            invocation.proceed();
        } else {
            CONTEXT.set(buildContext(invocationContext));
            try {
                invocation.proceed();
            } finally {
                CONTEXT.remove();
            }
        }
    }

    private static Map<String, Object> buildContext(ReflectiveInvocationContext<Method> method) {
        Map<String, Object> map = new HashMap<>();
        CONTEXT_PROVIDER_LOADER.selectAll(method).forEach(contextProvider4JUnit5 ->
                map.putAll(contextProvider4JUnit5.createContext(method))
        );
        appendContext(map, new MapContextUtil.Annotated() {
            @Override
            public <A extends Annotation> A getAnnotation(Class<A> aClass) {
                return method.getExecutable().getAnnotation(aClass);
            }
        }, () -> method.getExecutable().getName());
        return map;
    }
}
