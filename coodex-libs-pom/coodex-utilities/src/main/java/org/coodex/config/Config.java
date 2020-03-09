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
import org.coodex.util.Singleton;

import java.util.List;
import java.util.Properties;

/**
 * 基于System.getProperty实现Configuration，在provider中找不到时，则到系统属性中找
 */
public class Config {

    public final static AbstractConfiguration BASE_SYSTEM_PROPERTIES = new AbstractConfiguration() {

        @Override
        protected String search(String namespace, List<String> keys) {
            Properties properties = System.getProperties();
            for (String key : keys) {
                if (properties.containsKey(key)) return System.getProperty(key);
            }
            return null;
        }
    };

//    private static Singleton<Configuration> defaultConfiguration = new Singleton<Configuration>(
//            new Singleton.Builder<Configuration>() {
//                @Override
//                public Configuration build() {
//                    return new ConfigurationBaseProfile();
//                }
//            }
//    );

    private static LazyServiceLoader<DefaultConfigurationProvider> configurationProviderLazyServiceLoader =
            new LazyServiceLoader<DefaultConfigurationProvider>(new DefaultConfigurationProvider() {
                @Override
                public Configuration get() {
                    return new ConfigurationBaseProfile();
                }
            }) {
            };

    private static LazyServiceLoader<Configuration> configurationServiceLoader =
            new LazyServiceLoader<Configuration>(
                    new Singleton.Builder<Configuration>() {
                        @Override
                        public Configuration build() {
                            return configurationProviderLazyServiceLoader.get().get();
                        }
                    }
//                    new Singleton.Builder<ServiceLoader<Configuration>>() {
//                        @Override
//                        public ServiceLoader<Configuration> build() {
//                            return new ServiceLoaderImpl<Configuration>(
//                                    new ServiceLoaderImpl<DefaultConfigurationProvider>(new DefaultConfigurationProvider() {
//                                        @Override
//                                        public Configuration get() {
//                                            return new ConfigurationBaseProfile();
//                                        }
//                                    }) {
//                                    }.get()
//                                            .get()) {
//                            };
//                        }
//                    }
            ) {
            };


    public static Configuration getConfig() {
        return configurationServiceLoader.get();
    }

    public static String get(String key, String... namespaces) {
        String v = getConfig().get(key, namespaces);
        return v == null ? BASE_SYSTEM_PROPERTIES.get(key, namespaces) : v;
    }


    public static <T> T getValue(final String key, final T defaultValue, final String... namespace) {
        return getConfig().getValue(key, new Common.Supplier<T>() {
            @Override
            public T get() {
                return BASE_SYSTEM_PROPERTIES.getValue(key, defaultValue, namespace);

            }
        }, namespace);
    }

    public static <T> T getValue(final String key, final Common.Supplier<T> defaultValueSupplier, final String... namespace) {
        return getConfig().getValue(key, new Common.Supplier<T>() {
            @Override
            public T get() {
                return BASE_SYSTEM_PROPERTIES.getValue(key, defaultValueSupplier, namespace);
            }
        }, namespace);
    }


    public static String[] getArray(String key, String... namespaces) {
        return Common.toArray(get(key, namespaces), ",", (String[]) null);
    }

    public static String[] getArray(String key, String delim, String[] defaultValue, String... namespaces) {
        return Common.toArray(get(key, namespaces), delim, defaultValue);
    }

    public static String[] getArray(String key, String delim, Common.Supplier<String[]> supplier, String... namespaces) {
        return Common.toArray(get(key, namespaces), delim, supplier);
    }

}
