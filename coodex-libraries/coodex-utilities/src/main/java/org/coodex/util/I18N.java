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


    private static final ServiceLoader<TranslateService> TRANSLATE_SERVICE_SERVICE_LOADER
            = new LazyServiceLoader<TranslateService>(new ProfileBasedTranslateService()) {
    };


    public static TranslateService getTranslateService() {
        return TRANSLATE_SERVICE_SERVICE_LOADER.get();
    }

    public static String translate(String key) {
        return getTranslateService().translate(key);
    }

    public static String translate(String key, Locale locale) {
        return getTranslateService().translate(key, locale);
    }

    /**
     * 使用 {@link Renderer#render(String, Object...)} 接口对翻译后的内容进行渲染
     *
     * @param key     i18n key
     * @param objects 渲染参数
     * @return 渲染后的字符串
     */
    public static String render(String key, Object... objects) {
        return Renderer.render(translate(key), objects);
    }

    /**
     * 使用 {@link Renderer#render(String, Object...)} 接口对翻译后的内容进行渲染
     *
     * @param key     i18n key
     * @param locale  locale
     * @param objects 渲染参数
     * @return 渲染后的字符串
     */
    public static String render(String key, Locale locale, Object... objects) {
        return Renderer.render(translate(key, locale), objects);
    }


}
