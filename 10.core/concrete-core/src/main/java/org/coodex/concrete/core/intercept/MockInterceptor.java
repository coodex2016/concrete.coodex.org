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

package org.coodex.concrete.core.intercept;


import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.mockers.MockValue;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.message.GenericTypeHelper;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.core.intercept.InterceptOrders.OTHER;


@ServerSide
public class MockInterceptor extends AbstractSyncInterceptor {

    private Set<Class> exceptedClasses;

    public MockInterceptor() {
        this(new HashSet<>());
    }

    public MockInterceptor(Set<Class> exceptedClasses) {
        this.exceptedClasses = exceptedClasses;
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
        return !exceptedClasses.contains(context.getDeclaringClass());
    }

    public Set<Class> getExceptedClasses() {
        return exceptedClasses;
    }

    public void setExceptedClasses(Set<Class> exceptedClasses) {
        this.exceptedClasses = exceptedClasses;
    }

    @Override
    public Object around(DefinitionContext context, MethodInvocation joinPoint) throws Throwable {
        return run(context);
    }

    private Object run(DefinitionContext context) throws IOException {
        if (void.class.equals(context.getDeclaringMethod().getReturnType())) {
            return null;
        }

        MockValue mockValue = context.getAnnotation(MockValue.class);
        if (mockValue != null) {
            return JSONSerializerFactory.getInstance().parse(
                    getJson(mockValue),
                    GenericTypeHelper.toReference(
                            context.getDeclaringMethod().getReturnType(),
                            context.getDeclaringClass()
                    )
            );
        } else {
            return MockerFacade.mock(context.getDeclaringMethod());
        }
    }

    private String loadFromRes(String resourcePath) throws IOException {

        StringBuilder builder = new StringBuilder();
        URL resourceUrl = Common.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("resource not exists: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resourceUrl.openStream()
        ))) {
            reader.lines().forEachOrdered((line) -> builder.append(line));
        }
        return builder.toString();
    }

    private String getJson(MockValue value) throws IOException {
        String json = value.json();
        if (json.endsWith(".json")) {
            return loadFromRes(json);
        } else {
            return json;
        }
    }

    @Override
    public int getOrder() {
        return OTHER;
    }
}
