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

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

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

    private static final ServiceLoader<DefaultConfigurationProvider> configurationProviderLazyServiceLoader =
            new LazyServiceLoader<DefaultConfigurationProvider>(ConfigurationBaseProfile::new) {
            };

    private static final ServiceLoader<Configuration> configurationServiceLoader =
            new LazyServiceLoader<Configuration>(() -> configurationProviderLazyServiceLoader.get().get()) {

            };


    public static Configuration getConfig() {
        return configurationServiceLoader.get();
    }

    public static String get(String key, String... namespaces) {
        String v = getConfig().get(key, namespaces);
        return v == null ? BASE_SYSTEM_PROPERTIES.get(key, namespaces) : v;
    }


    public static <T> T getValue(final String key, final T defaultValue, final String... namespace) {
        return getConfig().getValue(key, () -> BASE_SYSTEM_PROPERTIES.getValue(key, defaultValue, namespace), namespace);
    }

    public static <T> T getValue(final String key, final Supplier<T> defaultValueSupplier, final String... namespace) {
        return getConfig().getValue(key, () -> BASE_SYSTEM_PROPERTIES.getValue(key, defaultValueSupplier, namespace), namespace);
    }


    public static String[] getArray(String key, String... namespaces) {
        return Common.toArray(get(key, namespaces), ",", (String[]) null);
    }

    public static String[] getArray(String key, String delim, String[] defaultValue, String... namespaces) {
        return Common.toArray(get(key, namespaces), delim, defaultValue);
    }

    public static String[] getArray(String key, String delim, Supplier<String[]> supplier, String... namespaces) {
        return Common.toArray(get(key, namespaces), delim, supplier);
    }

}
