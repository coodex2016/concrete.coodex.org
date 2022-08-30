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

package org.coodex.concrete.message.serializers;

import org.coodex.concrete.message.Serializer;
import org.coodex.util.JSONSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class JSonSerializer implements Serializer {

    private static final String JSON = "json";

    @Override
    public byte[] serialize(Serializable o) {
        return JSONSerializer.getInstance().toJson(o).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T extends Serializable> T deserialize(byte[] bytes, Type type) {
        return JSONSerializer.getInstance().parse(new String(bytes, StandardCharsets.UTF_8), type);
    }

    @Override
    public boolean accept(String param) {
        return JSON.equalsIgnoreCase(param);
    }
}
