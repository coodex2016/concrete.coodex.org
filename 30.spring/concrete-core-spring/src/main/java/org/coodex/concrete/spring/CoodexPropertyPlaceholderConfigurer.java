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

package org.coodex.concrete.spring;

import org.coodex.util.Common;
import org.coodex.util.RecursivelyProfile;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * 强化的PropertyPlaceHolder
 * <ul>
 * <li>支持namespace</li>
 * <li>支持默认值</li>
 * </ul>
 * 使用方式: ${namespace|key:defaultValue}
 * 参见 {@link org.coodex.util.RecursivelyProfile}
 * Created by davidoff shen on 2017-05-11.
 */
@Deprecated
public class CoodexPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {

        String namespace = null, key = placeholder, defaultValue = null;
        // 分离默认值
        int index = key.indexOf(':');
        if (index > 0) {
            defaultValue = key.substring(index + 1);
            key = key.substring(0, index);
        }

        //分离namespace
        index = key.indexOf('|');
        if (index > 0) {
            namespace = key.substring(0, index);
            key = key.substring(index + 1);
        }

        return Common.nullToStr(new RecursivelyProfile(props).getString(namespace, key, defaultValue));
    }
}
