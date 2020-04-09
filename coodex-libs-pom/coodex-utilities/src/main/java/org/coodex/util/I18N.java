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

public class I18N {

    private static Singleton<TranslateService> TRANSLATE_SERVICE_SINGLETON = Singleton.with(
            () -> new ServiceLoaderImpl<TranslateService>(new ProfileBasedTranslateService()) {
            }.get()
    );

    public static TranslateService getTranslateService() {
        return TRANSLATE_SERVICE_SINGLETON.get();
    }

    public static String translate(String key) {
        return getTranslateService().translate(key);
    }

    public static String translate(String key, Locale locale) {
        return getTranslateService().translate(key, locale);
    }
}
