/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

import java.lang.reflect.Type;

public interface JSONSerializer {

    ServiceLoader<JSONSerializer> JSON_SERIALIZER_LOADER
            = new LazyServiceLoader<JSONSerializer>() {
    };

    static JSONSerializer getInstance() {
        return JSON_SERIALIZER_LOADER.get();
    }

    <T> T parse(String json, Type t);

    default <T> T parse(Object jsonObject, Type t) {
        return jsonObject == null ? null : parse(toJson(jsonObject), t);
    }

    String toJson(Object t);
}