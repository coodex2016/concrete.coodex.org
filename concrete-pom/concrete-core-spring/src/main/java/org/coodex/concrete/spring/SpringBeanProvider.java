/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring;

import org.coodex.concrete.common.AbstractBeanProvider;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class SpringBeanProvider extends AbstractBeanProvider implements ApplicationContextAware {
    private final static Logger log = LoggerFactory.getLogger(SpringBeanProvider.class);


    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

////    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T getBean(String getName) {
//        return (T) context.getBean(getName);
//    }
//
////    @Override
//    public <T> T getBean(Class<T> type, String getName) {
//        return context.getBeansOfType(type).get(getName);
//    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        if (context == null) {
            log.info("spring bean provider not initialized, {} not load from spring bean provider.", type.getName());
            return new HashMap<>();
        } else {
            Map<String, T> map = new HashMap<>(context.getBeansOfType(type));
            for (Class c : collectionBeanTypes()) {
                //noinspection unchecked
                if (c.isAssignableFrom(type)) {
                    // 数组
                    Class arrayClass = Array.newInstance(type, 0).getClass();
                    //noinspection unchecked
                    Map<String, Object> x = context.getBeansOfType(arrayClass);
                    if (x != null && x.size() > 0) {
                        int index = 0;
                        for (Map.Entry<String, Object> entry : x.entrySet()) {
                            if (entry.getValue() == null) continue;
                            for (int l = 0, len = Array.getLength(entry.getValue()); l < len; l++) {
                                String key = entry.getKey() + "_array_" + index;
                                //noinspection unchecked
                                map.put(key, (T) Array.get(entry.getValue(), l));
                                index++;
                            }
                        }

                        if (index > 0) {
                            log.info("load {} {} bean(s) in array beans.", index, c.getName());
                        }
                    }

                    // 集合
                    Map<String, Collection> collectionBeans = context.getBeansOfType(Collection.class);
                    for (Map.Entry<String, Collection> entry : collectionBeans.entrySet()) {
                        int index = 0;
                        if (entry.getValue() == null || entry.getValue().size() == 0) continue;
                        for (Object o : entry.getValue()) {
                            if (o == null) continue;
                            //noinspection unchecked
                            if (c.isAssignableFrom(o.getClass())) {
                                String key = entry.getKey() + "_collection_" + index;
                                //noinspection unchecked
                                map.put(key, (T) o);
                                index++;
                            }
                        }

                        if (index > 0) {
                            log.info("load {} {} bean(s) in collection beans.", index, c.getName());
                        }
                    }
                }
            }
            return map;
        }
    }

    protected Class[] collectionBeanTypes() {
        return new Class[]{
                ConcreteInterceptor.class
        };
    }

}
