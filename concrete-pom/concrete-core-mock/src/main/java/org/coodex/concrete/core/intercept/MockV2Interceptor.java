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
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.mock.Mocker;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.coodex.concrete.core.intercept.InterceptOrders.MOCK;

@SuppressWarnings("unused")
@ServerSide
public class MockV2Interceptor extends AbstractSyncInterceptor {
    private final static Logger log = LoggerFactory.getLogger(MockV2Interceptor.class);

    private Set<Class<?>> exceptedClasses = new HashSet<>();
    private Set<Pattern> patterns = new HashSet<>();

    public MockV2Interceptor() throws IOException {
        this(new HashSet<>());
    }

    public MockV2Interceptor(Set<Class<?>> exceptedClasses) throws IOException {
        setExceptedClasses(exceptedClasses);
        loadExcepted();
    }

    private void loadExcepted() throws IOException {
        URL excepted = Common.getResource("mock.excepted");
        if (excepted == null) return;


        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(excepted.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (Common.isBlank(line)) continue;
                if (line.startsWith("#")) continue;

                try {
                    this.exceptedClasses.add(Class.forName(line));
                } catch (ClassNotFoundException e) {
                    this.patterns.add(Pattern.compile("^" + line + "$"));
                    log.info("ClassNotFound: {}, use regex", line);
                }
            }
        }
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
        boolean notMock = exceptedClasses.contains(context.getDeclaringClass());
        if (notMock) return false;
        String className = context.getDeclaringClass().getName();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(className).matches()) return false;
        }
        return true;
    }

    public Set<Class<?>> getExceptedClasses() {
        return exceptedClasses;
    }

    public void setExceptedClasses(Set<Class<?>> exceptedClasses) {
        if (exceptedClasses != null && exceptedClasses.size() > 0)
            this.exceptedClasses = new HashSet<>(exceptedClasses);
    }

    @Override
    public Object around(DefinitionContext context, MethodInvocation joinPoint) {
        return run(context);
    }

    private Object run(DefinitionContext context) {
        if (void.class.equals(context.getDeclaringMethod().getReturnType())) {
            return null;
        }

        return Mocker.mockMethod(
                context.getDeclaringMethod(),
                context.getDeclaringClass());

    }

    @Override
    public int getOrder() {
        return MOCK;
    }
}
