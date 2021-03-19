/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * 基于 {@link MessageFormat} 的渲染服务
 */
public class JavaTextMessageFormatRenderService implements RenderService {

    private static final Pattern PATTERN = Pattern.compile("\\{[0-9]+(,\\s*\\w+\\s*(,\\s*[^}]+)?)?\\s*}");

    @Override
    public String render(String template, Object... objects) {
        if(objects == null || objects.length == 0) return template;
        return MessageFormat.format(template, transfer(objects));
    }

    @Override
    public boolean accept(String param) {
        return param != null && PATTERN.matcher(param).find();
    }
}
