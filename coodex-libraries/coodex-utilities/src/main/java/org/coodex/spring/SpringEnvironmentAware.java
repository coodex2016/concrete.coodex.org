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

package org.coodex.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.inject.Named;
import java.util.Optional;

@Named
@Order(Integer.MIN_VALUE)
public class SpringEnvironmentAware implements SmartInstantiationAwareBeanPostProcessor {
    private final static Logger log = LoggerFactory.getLogger(SpringEnvironmentAware.class);
    private static Environment springEnvironment;
    private static boolean traced = false;

    public SpringEnvironmentAware(Environment environment) {
        springEnvironment = environment;
        log.info("coodex-spring: spring environment injected. {}",
                Optional.ofNullable(environment).map(Object::getClass).orElse(null));
    }

    public static Environment getSpringEnvironment() {
        if (springEnvironment == null) {
            if (log.isDebugEnabled()) {
                if (traced) return springEnvironment;
                synchronized (SpringEnvironmentAware.class) {
                    if (traced) return springEnvironment;
                    traced = true;
                    StringBuilder builder = new StringBuilder();
                    StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
                    for (StackTraceElement element : stackTraceElements) {
                        builder.append("\n\tat ").append(element);
                    }
                    log.debug("spring environment not injected yet.{}", builder);
                }
            }
        }
        return springEnvironment;
    }
}
