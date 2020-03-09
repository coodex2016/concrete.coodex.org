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

package org.coodex.concrete.common;

import org.coodex.util.ServiceLoaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ConcreteServiceLoaderProvider implements ServiceLoaderProvider {
    private final static Logger log = LoggerFactory.getLogger(ConcreteServiceLoaderProvider.class);

    @Override
    public <S> Map<String, S> load(Class<S> serviceClass) {
        BeanProvider beanProvider = BeanServiceLoaderProvider.getBeanProvider();
        if (beanProvider != null) {
            return beanProvider.getBeansOfType(serviceClass);
        } else {
            log.warn("BeanProvider NOT initialized.", new RuntimeException("BeanProvider NOT initialized."));
            return new HashMap<>();
        }
    }
}
