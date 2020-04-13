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
import org.coodex.util.Common;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;

import static org.coodex.util.Common.cast;

public class DefaultSerializer implements Serializer {
    @Override
    public byte[] serialize(Serializable o) {
        try {
            return Common.serialize(o);
        } catch (IOException e) {
            throw Common.rte(e);
        }
    }

    @Override
    public <T extends Serializable> T deserialize(byte[] bytes, Type type) {
        try {
            return cast(Common.deserialize(bytes));
        } catch (Throwable throwable) {
            throw Common.rte(throwable);
        }
    }

    @Override
    public boolean accept(String param) {
        return false;
    }
}
