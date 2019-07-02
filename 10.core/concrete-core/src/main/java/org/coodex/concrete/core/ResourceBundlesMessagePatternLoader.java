/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.I18NFacade;
import org.coodex.concrete.common.MessagePatternLoader;
import org.coodex.concrete.common.TranslateService;
import org.coodex.config.Config;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * 基于 ResourceBundle 的实现
 * Created by davidoff shen on 2016-12-02.
 * @deprecated (统一使用I18N)
 */
@Deprecated
public class ResourceBundlesMessagePatternLoader implements MessagePatternLoader {

    public ResourceBundlesMessagePatternLoader() {
//        TranslateService translateService = I18NFacade.getTranslateService();
//        if(translateService instanceof ResourceBundlesTranslateService){
//            //noinspection deprecation
//            ((ResourceBundlesTranslateService) translateService).add(
//                    Config.getArray("messagePattern.resourceBundles", ",", MESSAGE_PATTERN, getAppSet())
//            );
//        }
    }

    public static final String[] MESSAGE_PATTERN = new String[]{"messagePattern"};


    private String getPatternFromBundle(String key) {
//        String[] list = Config.getArray("messagePattern.resourceBundles", ",", MESSAGE_PATTERN, getAppSet());
//        if (list == null || list.length == 0) return null;
//        for (String resource : list) {
//            Locale locale = getServiceContext() == null ? null : getServiceContext().getLocale();//.get();
//            if (locale == null)
//                locale = Locale.getDefault();
//
//            if (Common.isBlank(resource) || Common.isBlank(resource.trim())) continue;
//            try {
//                String pattern = ResourceBundle.getBundle(resource, locale).getString(key);
//                if (pattern != null) return pattern;
//            } catch (Throwable t) {
//            }
//        }
        return I18NFacade.translate(key);
    }


    @Override
    public String getMessageTemplate(String key) {
        return getPatternFromBundle(key);
    }
}
