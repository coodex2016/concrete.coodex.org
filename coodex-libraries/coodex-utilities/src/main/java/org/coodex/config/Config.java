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

package org.coodex.config;

import org.coodex.util.Common;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author davidoff shen
 */
public class Config {

    public final static SystemPropertiesConfiguration BASE_SYSTEM_PROPERTIES = new SystemPropertiesConfiguration();
    private final static Logger log = LoggerFactory.getLogger(Config.class);
    private static final Singleton<WrappedConfiguration> WRAPPED_CONFIGURATION = Singleton.with(
            () -> new WrappedConfiguration(new LazyServiceLoader<Configuration>(
                    () -> new LazyServiceLoader<DefaultConfigurationProvider>(ConfigurationBaseProfile::new) {
                    }.get().get()) {
            })
    );

    public static Configuration getConfig() {
        return WRAPPED_CONFIGURATION.get();
    }

    public static String get(String key, String... namespaces) {
        return getConfig().get(key, namespaces);
    }

    public static <T> T getValue(final String key, final T defaultValue, final String... namespace) {
        return getConfig().getValue(key, defaultValue, namespace);
    }

    public static <T> T getValue(final String key, final Supplier<T> defaultValueSupplier, final String... namespace) {
        return getConfig().getValue(key, defaultValueSupplier, namespace);
    }

    public static String[] getArray(String key, String... namespaces) {
        return getArray(key, () -> null, namespaces);
    }

    public static String[] getArray(String key, String[] defaultValue, String... namespaces) {
        return getArray(key, () -> defaultValue, namespaces);
    }

    public static String[] getArray(String key, Supplier<String[]> supplier, String... namespaces) {
//        return getValue(key, supplier, namespaces);
        return Common.toArray(get(key, namespaces), ",", supplier);
    }

    @Deprecated
    public static String[] getArray(String key, String delim, String[] defaultValue, String... namespaces) {
        return Common.toArray(get(key, namespaces), delim, defaultValue);
    }

    @Deprecated
    public static String[] getArray(String key, String delim, Supplier<String[]> supplier, String... namespaces) {
        return Common.toArray(get(key, namespaces), delim, supplier);
    }

    public static class SystemPropertiesConfiguration extends AbstractConfiguration {
        @Override
        protected String search(String namespace, List<String> keys) {
            Properties properties = System.getProperties();
            for (String key : keys) {
                if (properties.containsKey(key)) {
                    return System.getProperty(key);
                }
            }
            return null;
        }
    }

    private static class WrappedConfiguration extends AbstractConfiguration {
        private final Singleton<List<Configuration>> configuration;

        private WrappedConfiguration(ServiceLoader<Configuration> configurationServiceLoader) {
            this.configuration = Singleton.with(() -> {
                List<Configuration> configurationList = new ArrayList<>(configurationServiceLoader.sorted());
                Configuration defaultConfiguration = configurationServiceLoader.getDefault();
                if (defaultConfiguration != null) {
                    configurationList.add(configurationServiceLoader.getDefault());
                }

                configurationList.add(BASE_SYSTEM_PROPERTIES);
                return configurationList;
            });
        }

        @Override
        protected String search(String namespace, List<String> keys) {
            for (Configuration c : configuration.get()) {
                for (String key : keys) {
                    String v = c.get(key, namespace);
                    if (v != null) {
                        if (Common.isDebug() && log.isDebugEnabled()) {
                            if (namespace == null) {
                                log.debug("load config: {}={} by {}", key, v, c.getClass());
                            } else {
                                log.debug("load config: {}.{}={}; by {}", namespace, key, v, c.getClass());
                            }
                        }
                        return v;
                    }
                }
            }
            return null;
        }

    }

}
