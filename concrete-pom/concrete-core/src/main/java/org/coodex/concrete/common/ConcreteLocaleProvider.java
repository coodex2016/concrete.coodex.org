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

package org.coodex.concrete.common;

import org.coodex.util.DefaultLocaleProvider;

import java.util.Locale;
import java.util.Optional;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;

public class ConcreteLocaleProvider implements DefaultLocaleProvider {

    private static Locale locale = Locale.getDefault();

    public static void setDefaultLocale(Locale locale) {
        ConcreteLocaleProvider.locale = locale;
    }

    public static Locale getLocalDefault() {
        return Optional.ofNullable(getServiceContext())
                .map(ServiceContext::getLocale)
                .orElse(Optional.ofNullable(locale).orElseGet(Locale::getDefault));
    }

    @Override
    public Locale getDefault() {
        return getLocalDefault();
    }
}
