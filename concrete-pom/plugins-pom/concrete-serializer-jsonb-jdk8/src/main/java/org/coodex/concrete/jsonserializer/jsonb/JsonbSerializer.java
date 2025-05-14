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

import org.coodex.concrete.common.ConcreteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class JsonbSerializer extends org.coodex.util.json.JsonbSerializer implements org.coodex.concrete.common.JSONSerializer /*extends
AbstractJsonSerializer */ {
    private static final Logger log = LoggerFactory.getLogger(JsonbSerializer.class);

    static {
        log.warn("use org.coodex:coodex-jsonserializer-jsonb-v1:{} instead.", ConcreteHelper.VERSION);
    }

//    private final Singleton<Jsonb> jsonbSingleton = Singleton.with(() -> {
//        String providerName = Config.get("jsonb.provider", getAppSet());
//        return Common.isBlank(providerName) ? JsonbBuilder.create() : JsonbBuilder.newBuilder(providerName).build();
//    });
//
//    private Jsonb getInstance() {
//        return jsonbSingleton.get();
//    }
//
//    @Override
//    public <T> T parse(String json, Type t) {
//        if (Common.isBlank(json)) return null;
//        try {
//            return /*String.class.equals(t) ? Common.cast(json) : */getInstance().fromJson(json, t);
//        } catch (Throwable th) {
//            throw Common.rte(th);
//        }
//    }
//
//    @Override
//    public String toJson(Object t) {
//        return getInstance().toJson(t);
//    }
}
