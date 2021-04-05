/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring.components;

import org.coodex.config.AbstractConfiguration;
import org.coodex.util.SPI;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import javax.inject.Named;
import java.util.List;

@Named
@SPI.Ordered(0)
public class ConfigurationBaseSpringPropertySources extends AbstractConfiguration implements ApplicationContextAware {
    private Environment springEnvironment;


    @Override
    protected String search(String namespace, List<String> keys) {
        if (springEnvironment != null) {
            for (String key : keys) {
                String v = springEnvironment.getProperty(key);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springEnvironment = applicationContext.getEnvironment();
    }
}
