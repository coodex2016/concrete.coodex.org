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

package org.coodex.concrete.serializer.jackson2;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.json.Jackson2JSONSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class Jackson2Serializer extends Jackson2JSONSerializer implements org.coodex.concrete.common.JSONSerializer {
    private final static Logger log = LoggerFactory.getLogger(Jackson2Serializer.class);

    static {
        log.warn("use org.coodex:coodex-jsonserializer-jackson2:{} instead.", ConcreteHelper.VERSION);
    }


    //    private ObjectMapper mapper;
//    private final Singleton<ObjectMapper> mapperSingleton = Singleton.with(() -> {
//        ObjectMapper mapper = new ObjectMapper();
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//        SimpleModule simpleModule = new SimpleModule();
//        // todo register serializer and deserializer
//        if (atomicInteger.get() > 0) {
//            mapper.registerModule(simpleModule);
//        }
//        return mapper;
//    });
//
//    private ObjectMapper getMapper() {
//        return mapperSingleton.get();
//    }
//
//    @Override
//    public <T> T parse(String json, Type t) {
//        if (Common.isBlank(json)) return null;
//        try {
//            if (t instanceof Class) {
////                if (String.class.equals(t)) {
////                    return Common.cast(json);
////                } else {
//                Class<T> c = Common.cast(t);
//                return getMapper().readValue(json, c);
////                }
//            } else {
//                return getMapper().readValue(json, TypeFactory.defaultInstance().constructType(t));
//            }
//        } catch (Throwable th) {
//            throw th instanceof RuntimeException ? (RuntimeException) th : new RuntimeException(th);
//        }
//    }
//
//    @Override
//    public String toJson(Object t) {
//        try {
//            return getMapper().writeValueAsString(t);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
