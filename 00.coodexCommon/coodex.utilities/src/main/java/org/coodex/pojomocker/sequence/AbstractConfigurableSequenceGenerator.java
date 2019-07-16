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

package org.coodex.pojomocker.sequence;

import org.coodex.config.Config;
import org.coodex.config.Configuration;
import org.coodex.util.Singleton;
@Deprecated
public abstract class AbstractConfigurableSequenceGenerator<T> extends AbstractSequenceGenerator<T> {


    private Singleton<Configuration> configSingleton = new Singleton<Configuration>(new Singleton.Builder<Configuration>() {
        @Override
        public Configuration build() {

            return new Configuration() {
                private String[] contextNameSpace = getNameSpace();

                @Override
                public String get(String key, String... namespaces) {
                    if (namespaces == null || namespaces.length == 0) {
                        return Config.get(key, contextNameSpace);
                    } else {
                        return Config.get(key, namespaces);
                    }
                }

                @Override
                public <T> T getValue(String key, T defaultValue, String... namespace) {
                    if (namespace == null || namespace.length == 0) {
                        return Config.getValue(key, defaultValue, contextNameSpace);
                    } else {
                        return Config.getValue(key, defaultValue, namespace);
                    }
                }
            };
        }
    });

    private String[] getNameSpace() {
        NameSpace nameSpace = this.getClass().getAnnotation(NameSpace.class);
        return nameSpace == null ? new String[]{"seq_gen"} : new String[]{"seq_gen", nameSpace.value()};
    }

    //    protected Profile getProfile() {
//        return profileSingleton.getInstance();
//    }
    protected Configuration getConfig() {
        return configSingleton.getInstance();
    }


}
