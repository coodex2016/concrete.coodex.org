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

import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.message.Serializer;
import org.coodex.util.Common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class JSonSerializer implements Serializer {

    private static final String JSON = "json";

    @Override
    public byte[] serialize(Serializable o) {
        try {
            return JSONSerializerFactory.getInstance().toJson(o).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Common.rte(e);
        }
    }

    @Override
    public <T extends Serializable> T deserialize(byte[] bytes, Type type) {

        try {
            return JSONSerializerFactory.getInstance().parse(new String(bytes, "UTF-8"), type);
        } catch (UnsupportedEncodingException e) {
            throw Common.rte(e);
        }
    }

    @Override
    public boolean accept(String param) {
        return JSON.equalsIgnoreCase(param);
    }
}
