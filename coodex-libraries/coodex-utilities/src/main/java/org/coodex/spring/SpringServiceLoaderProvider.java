/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import org.coodex.util.AbstractServiceLoaderProvider;
import org.coodex.util.SPI;
import org.coodex.util.Singleton;
import org.coodex.util.SingletonMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SPI.Ordered(0)
public class SpringServiceLoaderProvider extends AbstractServiceLoaderProvider implements ApplicationContextAware {
    private static ApplicationContext CONTEXT;

    @Override
    protected Map<String, Object> loadByRowType(Class<?> rowType) {
        return Optional.ofNullable(CONTEXT)
                .map(applicationContext -> new HashMap<String, Object>(
                        applicationContext.getBeansOfType(rowType)
                ))
                .orElse(new HashMap<>());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
        Singleton.resetAll();
        SingletonMap.resetAll();
    }
}
