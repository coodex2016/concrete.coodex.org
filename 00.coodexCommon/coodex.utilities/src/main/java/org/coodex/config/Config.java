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
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;

public class Config {

    private static Singleton<Configuration> defaultConfiguration = new Singleton<Configuration>(
            new Singleton.Builder<Configuration>() {
                @Override
                public Configuration build() {
                    return new ConfigurationBaseProfile();
                }
            }
    );
    private static ServiceLoader<Configuration> configurationServiceLoader = new ServiceLoaderImpl<Configuration>() {
        @Override
        public Configuration getDefault() {
            return defaultConfiguration.get();
        }
    };

    public static Configuration getConfig() {
        return configurationServiceLoader.get();
    }

    public static String get(String key, String... namespaces) {
        return getConfig().get(key, namespaces);
    }

    public static <T> T getValue(String key, T defaultValue, String... namespace) {
        return getConfig().getValue(key, defaultValue, namespace);
    }

    public static String[] getArray(String key, String... namespaces) {
        return Common.toArray(getConfig().get(key, namespaces), ",", (String[]) null);
    }

    public static String[] getArray(String key, String delim, String[] defaultValue, String... namespaces) {
        return Common.toArray(getConfig().get(key, namespaces), delim, defaultValue);
    }

}
