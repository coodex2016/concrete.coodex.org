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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.coodex.concrete.common.AbstractJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class Jackson2Serializer extends AbstractJsonSerializer {

    private final static Logger log = LoggerFactory.getLogger(Jackson2Serializer.class);

    private ObjectMapper mapper;

    private synchronized ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(String json, Type t) {
        try {
            if (t instanceof Class) {
                return String.class.equals(t) ? (T) json : getMapper().readValue(json, (Class<T>) t);
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
