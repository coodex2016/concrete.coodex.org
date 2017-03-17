/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.concrete.spring;

import org.coodex.concrete.common.AbstractBeanProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class SpringBeanProvider extends AbstractBeanProvider implements ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

////    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T getBean(String name) {
//        return (T) context.getBean(name);
//    }
//
////    @Override
//    public <T> T getBean(Class<T> type, String name) {
//        return context.getBeansOfType(type).get(name);
//    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return context.getBeansOfType(type);
    }

}
