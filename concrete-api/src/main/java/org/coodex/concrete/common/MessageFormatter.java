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

package org.coodex.concrete.common;

/**
 * Created by davidoff shen on 2016-09-04.
 *
 * @deprecated 2020-05-06，使用 {@link org.coodex.util.Renderer}替代
 */
@Deprecated
public interface MessageFormatter {

    String format(String pattern, Object... objects);

    String getNamespace();

//    String formatByKey(String key, Object ... objects);

//    String getMessageTemplate(String key);
}
