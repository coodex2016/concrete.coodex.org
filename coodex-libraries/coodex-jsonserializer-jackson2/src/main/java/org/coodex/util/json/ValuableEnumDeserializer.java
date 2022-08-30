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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import org.coodex.util.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

public class ValuableEnumDeserializer extends JsonDeserializer<Enum<?>> implements ContextualDeserializer {

    private final SingletonMap<Class<?>, Deserializer> actualDeserializers = SingletonMap
            .<Class<?>, Deserializer>builder()
            .function(Deserializer::new)
            .build();
    private final SingletonMap<Class<?>, EnumDeserializer> deserializers;

    public ValuableEnumDeserializer(SingletonMap<Class<?>, EnumDeserializer> deserializers) {
        this.deserializers = deserializers;
    }

    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        Class<?> enumClass = ctxt.getContextualType().getRawClass();
        if (Valuable.class.isAssignableFrom(enumClass)) {
            return actualDeserializers.get(enumClass);
        } else
            return deserializers.get(enumClass);
    }

    static class Deserializer extends JsonDeserializer<Object> {
        private final Class<Valuable<?>> enumClass;

        Deserializer(Class<?> enumClass) {this.enumClass = Common.cast(enumClass);}

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            Type pt = GenericTypeHelper.solveFromType(Valuable.class.getTypeParameters()[0], enumClass);
            Object o = p.readValueAs(new TypeReference<Valuable<?>>() {
                @Override
                public Type getType() {
                    return pt;
                }
            });
            for (Valuable<?> e : enumClass.getEnumConstants()) {
                if (Objects.equals(e.getValue(), o)) return e;
            }
            throw new NonEnumElementException(enumClass, o);
        }
    }
}

