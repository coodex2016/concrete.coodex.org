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
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.annotation.Order;

import javax.inject.Named;
import java.util.Optional;

@Named
@Order(Integer.MIN_VALUE)
public class SpringBeanFactoryAware implements SmartInstantiationAwareBeanPostProcessor {
    private final static Logger log = LoggerFactory.getLogger(SpringBeanFactoryAware.class);
    private static ListableBeanFactory listableBeanFactory;
    private static boolean traced = false;

    public SpringBeanFactoryAware(ListableBeanFactory beanFactory) {
        listableBeanFactory = beanFactory;
        log.info("coodex-spring: listable bean factory injected. {}",
                Optional.ofNullable(beanFactory).map(Object::getClass).orElse(null));
    }

    public static ListableBeanFactory getListableBeanFactory() {
        if (listableBeanFactory == null) {
            if (log.isDebugEnabled()) {
                if (traced) return listableBeanFactory;

                synchronized (SpringBeanFactoryAware.class) {
                    if (traced) return listableBeanFactory;
                    StringBuilder builder = new StringBuilder();
                    StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
                    for (StackTraceElement element : stackTraceElements) {
                        builder.append("\n\tat ").append(element);
                    }
                    log.debug("spring bean factory not injected yet.{}", builder);
                    traced = true;
                }

            }
        }
        return listableBeanFactory;
    }
}
