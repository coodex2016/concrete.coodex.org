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
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

import static org.coodex.junit.enhance.MapContextUtil.appendContext;
import static org.coodex.junit.enhance.TestUtils.CONTEXT;

public class CoodexEnhanceTestRule implements TestRule {

    private static final LazySelectableServiceLoader<Description, ContextProvider4JUnit4> CONTEXT_PROVIDER_LOADER =
            new LazySelectableServiceLoader<Description, ContextProvider4JUnit4>(new ContextProvider4JUnit4() {
                @Override
                public Map<String, Object> createContext(Description description) {
                    return MapContextUtil.buildMapContext(description::getAnnotation);
                }

                @Override
                public boolean accept(Description param) {
                    return param.getAnnotation(MapContext.class) != null || param.getAnnotation(Entry.class) != null;
                }
            }) {
            };


    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (CONTEXT.get() != null) {
                    statement.evaluate();
                } else {
                    CONTEXT.set(buildContext(description));
                    try {
                        statement.evaluate();
                    } finally {
                        CONTEXT.remove();
                    }
                }
            }
        };
    }

    private static Map<String, Object> buildContext(Description description) {
        Map<String, Object> map = new HashMap<>();
        CONTEXT_PROVIDER_LOADER.selectAll(description).forEach(contextProvider4JUnit4 ->
                map.putAll(contextProvider4JUnit4.createContext(description))
        );
        appendContext(map, description::getAnnotation, description::getMethodName);
        return map;
    }
}
