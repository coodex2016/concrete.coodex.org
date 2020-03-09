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

package org.coodex.util;

import java.util.Locale;

public abstract class AbstractTranslateService implements TranslateService {

    private static ServiceLoader<DefaultLocaleProvider> DEFAULT_LOCATE_PROVIDER_LOADER = new ServiceLoaderImpl<DefaultLocaleProvider>(
            new DefaultLocaleProvider() {
                @Override
                public Locale getDefault() {
                    return Locale.getDefault();
                }
            }
    ) {
    };

    @Override
    public String translate(String key) {
        return translate(key, null);
    }

    @Override
    public String translate(String key, Locale locale) {
        locale = locale == null ? DEFAULT_LOCATE_PROVIDER_LOADER.get().getDefault() : locale;
        String toSearch = key;
        if (key.startsWith("{") && key.endsWith("}")) {
            toSearch = Common.trim(key, "{ \r\n\t}");
        }
        String result = translateIfExits(toSearch, locale);
        return result == null ? key : result;
    }

    protected abstract String translateIfExits(String key, Locale locale);
}
