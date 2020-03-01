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

import java.net.URL;

public interface ProfileProvider extends SelectableService<URL>, Comparable<ProfileProvider> {

    /**
     * @return 此ProfileProvider支持的文件名后缀
     */
    String[] getSupported();


    /**
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * @param url profile url
     * @return 获得Profile示例
     */
    Profile get(URL url);

    /**
     * @return 支持优先级，值越大越优先
     */
    int priority();
}
