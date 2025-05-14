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

package org.coodex.mock;

import org.coodex.id.IDGenerator;
import org.coodex.util.SingletonMap;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class IdMockerProvider extends AbstractTypeMocker<Mock.ID> {
    private static final Class<?>[] SUPPORTED_CLASSES = new Class[]{
            String.class, int.class, Integer.class, long.class, Long.class
    };

    private static final SingletonMap<String, AtomicLong> ID_SEQ_MAP = SingletonMap.<String, AtomicLong>builder()
            .function(key -> new AtomicLong(0))
            .build();

    @Override
    protected Class<?>[] getSupportedClasses() {
        return SUPPORTED_CLASSES;
    }

    @Override
    protected boolean accept(Mock.ID annotation) {
        return true;
    }

    @Override
    public Object mock(Mock.ID mockAnnotation, Type targetType) {
        if (Objects.equals(targetType, String.class)) {
            return IDGenerator.newId();
        } else if (Objects.equals(targetType, int.class) || Objects.equals(targetType, Integer.class)) {
            return (int) ID_SEQ_MAP.get(mockAnnotation.group()).incrementAndGet();
        } else
            return ID_SEQ_MAP.get(mockAnnotation.group()).incrementAndGet();
    }
}
