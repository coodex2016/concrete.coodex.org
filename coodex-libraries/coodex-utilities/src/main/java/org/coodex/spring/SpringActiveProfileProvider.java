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

import org.coodex.util.ActiveProfilesProvider;
import org.coodex.util.SPI;
import org.coodex.util.Singleton;
import org.coodex.util.SingletonMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Optional;

@SPI.Ordered(0)
public class SpringActiveProfileProvider implements ActiveProfilesProvider, ApplicationContextAware {
    private static ApplicationContext APPLICATION_CONTEXT;

    @PostConstruct
    public void postConstruct(){
        Singleton.resetAll();
        SingletonMap.resetAll();
    }

    @Override
    public String[] getActiveProfiles() {
        return Optional.ofNullable(APPLICATION_CONTEXT)
                .map(ApplicationContext::getEnvironment)
                .map(Environment::getActiveProfiles)
                .orElse(new String[0]);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        APPLICATION_CONTEXT = applicationContext;
    }
}
