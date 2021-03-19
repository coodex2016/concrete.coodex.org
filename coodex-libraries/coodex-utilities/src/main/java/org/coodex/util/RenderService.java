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

import java.util.function.Supplier;

/**
 * 字符串渲染器，根据模板进行选择
 */
public interface RenderService extends SelectableService<String> {

    default Object[] transfer(Object... objects) {
        if (objects == null) return null;
        if (objects.length == 0) return objects;
        Object[] result = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            result[i] = o instanceof Supplier ? ((Supplier<?>) o).get() : o;
        }
        return result;
    }

    String render(String template, Object... objects);
}
