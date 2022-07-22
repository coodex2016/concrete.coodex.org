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

package org.coodex.concrete.jsonserializer.jsonb;

import org.coodex.concrete.common.JSONSerializer;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.Singleton;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.lang.reflect.Type;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class JsonbSerializer implements JSONSerializer /*extends AbstractJsonSerializer */ {

    private final Singleton<Jsonb> jsonbSingleton = Singleton.with(() -> {
        String providerName = Config.get("jsonb.provider", getAppSet());
        return Common.isBlank(providerName) ? JsonbBuilder.create() : JsonbBuilder.newBuilder(providerName).build();
    });

    private Jsonb getInstance() {
        return jsonbSingleton.get();
    }

    @Override
    public <T> T parse(String json, Type t) {
        if (json == null) return null;
        try {
            return /*String.class.equals(t) ? Common.cast(json) : */getInstance().fromJson(json, t);
        } catch (Throwable th) {
            throw Common.rte(th);
        }
    }

    @Override
    public String toJson(Object t) {
        return getInstance().toJson(t);
    }
}
