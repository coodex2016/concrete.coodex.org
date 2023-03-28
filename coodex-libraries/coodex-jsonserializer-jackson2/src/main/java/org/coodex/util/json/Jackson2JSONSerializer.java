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

package org.coodex.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.EnumResolver;
import com.fasterxml.jackson.databind.util.EnumValues;
import org.coodex.config.Config;
import org.coodex.util.*;

import java.io.IOException;
import java.lang.reflect.Type;

public class Jackson2JSONSerializer implements JSONSerializer {

    //    private static final Logger log = LoggerFactory.getLogger(Jackson2JSONSerializer.class);
    private static final Singleton<ObjectMapper> mapperSingleton = Singleton.with(() -> {
        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        boolean failedOnUnknownProperties =  Config.getValue("jsonserializer.jackson.failedOnUnknownProperties", false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failedOnUnknownProperties);
        if(!failedOnUnknownProperties){

        }
//
        SingletonMap<Class<Enum<?>>, EnumSerializer> serializerMap = SingletonMap
                .<Class<Enum<?>>, EnumSerializer>builder()
                .function(c -> new EnumSerializer(EnumValues.construct(mapper.getSerializationConfig(), c),
                        mapper.isEnabled(SerializationFeature.WRITE_ENUMS_USING_INDEX)))
                .build();

        SingletonMap<Class<?>, EnumDeserializer> deserializerMap = SingletonMap
                .<Class<?>, EnumDeserializer>builder()
                .function(c -> {
                    EnumResolver enumResolver = EnumResolver
                            .constructUsingToString(mapper.getDeserializationConfig(), c);
                    return new EnumDeserializer(enumResolver, mapper.getDeserializationConfig()
                            .isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS));
                })
                .build();


        SimpleModule simpleModule = new SimpleModule();

        //noinspection rawtypes
        simpleModule.addSerializer(Enum.class, new JsonSerializer<Enum>() {
            @Override
            public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else if (value instanceof Valuable) {
                    gen.writeObject(((Valuable<?>) value).getValue());
                } else {
                    serializerMap.get(Common.cast(value.getClass())).serialize(value, gen, serializers);
                }
            }
        }).addDeserializer(Enum.class, new ValuableEnumDeserializer(deserializerMap));

        mapper.registerModule(simpleModule);
        return mapper;
    });

    public static ObjectMapper getMapper() {
        return mapperSingleton.get();
    }


    @Override
    public <T> T parse(String json, Type t) {
        if (Common.isBlank(json)) return null;
        try {
            if (t instanceof Class) {
//                if (String.class.equals(t)) {
//                    return Common.cast(json);
//                } else {
                Class<T> c = Common.cast(t);
                return getMapper().readValue(json, c);
//                }
            } else {
                return getMapper().readValue(json, TypeFactory.defaultInstance().constructType(t));
            }
        } catch (Throwable th) {
            throw th instanceof RuntimeException ? (RuntimeException) th : new RuntimeException(th);
        }
    }

    @Override
    public String toJson(Object t) {
        try {
            return getMapper().writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}


