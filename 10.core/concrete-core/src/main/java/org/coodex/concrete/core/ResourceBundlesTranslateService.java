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

package org.coodex.concrete.core;

import org.coodex.concrete.common.TranslateService;
import org.coodex.config.Config;
import org.coodex.util.Common;

import java.util.*;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class ResourceBundlesTranslateService implements TranslateService {

    private Set<String> resources = new HashSet<>();

    public ResourceBundlesTranslateService() {
        resources.addAll(
                Arrays.asList(
                        Config.getArray("i18n.resource", ",", new String[0], getAppSet())
                )
        );
        // 适配ResourceBundlesMessagePatternLoader
        resources.addAll(
                Arrays.asList(
                        Config.getArray("messagePattern.resourceBundles", ",", new String[]{"messagePattern"}, getAppSet())
                )
        );

    }

    /**
     * @param resoruceBundles
     * @deprecated (仅为适应ResourceBundlesMessagePatternLoader之前的定义)
     */
    @Deprecated
    void add(String[] resoruceBundles) {
        this.resources.addAll(Arrays.asList(resoruceBundles));
    }

    @Override
    public String translate(String key) {
        if(key.startsWith("{") && key.endsWith("}")) {
            String toSearch = Common.trim(key,"{ \r\n\t}");
            for (String resource : resources) {
                if (Common.isBlank(resource) || Common.isBlank(resource.trim())) continue;

                Locale locale = getServiceContext() == null ? null : getServiceContext().getLocale();

                if (locale == null) {
                    locale = Locale.getDefault();
                }

                String result = null;
                try {
                    result = ResourceBundle.getBundle(resource, locale).getString(toSearch);
                } catch (Throwable t) {
                    // do nothing
                }
                if (result != null)
                    return result;
            }
        }
        return key;
    }
}
