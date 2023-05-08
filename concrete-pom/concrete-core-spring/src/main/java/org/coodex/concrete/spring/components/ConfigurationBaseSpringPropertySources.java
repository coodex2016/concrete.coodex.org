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

package org.coodex.concrete.spring.components;

import org.coodex.config.AbstractConfiguration;
import org.coodex.spring.SpringEnvironmentAware;
import org.coodex.util.Common;
import org.coodex.util.SPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.StringJoiner;

@SPI.Ordered(0)
public class ConfigurationBaseSpringPropertySources extends AbstractConfiguration
        /*implements ApplicationContextAware*/ {
    private final static Logger log = LoggerFactory.getLogger(ConfigurationBaseSpringPropertySources.class);
//    private Environment springEnvironment;
    private static boolean traced = false;



    @Override
    protected String search(String namespace, List<String> keys) {
        String prefix = namespace == null ? "" : (namespace + ".");

        String v = find(keys, "concrete." + prefix);
        if (v != null) {
            return v;
        }
        return find(keys, prefix);
    }

    private String find(List<String> keys, String prefix) {
        Environment springEnvironment = SpringEnvironmentAware.getSpringEnvironment();
        if (springEnvironment != null) {
            for (String key : keys) {
                if (springEnvironment.containsProperty(prefix + key)) {
                    String v = springEnvironment.getProperty(prefix + key);
                    if (v != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("load config: {}{}={} from SpringPropertySources", prefix, key, v);
                        }
                        return v;
                    }
                } else if (springEnvironment.containsProperty(prefix + key + "[0]")) {
                    // list type
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 0; springEnvironment.containsProperty(prefix + key + "[" + i + "]"); i++) {
                        String str = springEnvironment.getProperty(prefix + key + "[" + i + "]");// NOSONAR
                        if (!Common.isBlank(str)) {
                            joiner.add(str.trim());// NOSONAR
                        }
                    }
                    String v = joiner.toString();
                    if (log.isDebugEnabled()) {
                        log.debug("load config: {}{}={} from SpringPropertySources[list type]", prefix, key, v);
                    }
                    return v;
                }
            }
        } else {
            if(!traced) {
                synchronized (ConfigurationBaseSpringPropertySources.class) {
                    if(!traced) {
                        log.warn("spring environment not injected yet.");
                        traced = true;
                    }
                }
            }
        }
        return null;
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        springEnvironment = applicationContext.getEnvironment();
//    }
}
