/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.jsonserializer.jsonb;

import org.coodex.concrete.common.AbstractJsonSerializer;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.Common;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.lang.reflect.Type;

public class JsonbSerializer extends AbstractJsonSerializer {

    private Jsonb jsonbInstnace = null;

    private synchronized Jsonb getInstance() {
        if (jsonbInstnace == null) {
            String providerName = ConcreteHelper.getProfile().getString("jsonb.provider", null);
            jsonbInstnace = Common.isBlank(providerName) ? JsonbBuilder.create() : JsonbBuilder.newBuilder(providerName).build();
        }
        return jsonbInstnace;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(String json, Type t) {
        try {
            return String.class.equals(t) ? (T) json : getInstance().fromJson(json, t);
        } catch (Throwable th) {
            throw th instanceof RuntimeException ? (RuntimeException) th : new RuntimeException(th);
        }
    }

    @Override
    public String toJson(Object t) {
        return getInstance().toJson(t);
    }
}
