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

package org.coodex.junit.enhance;

import org.coodex.util.Clock;
import org.coodex.util.Common;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.coodex.junit.enhance.TestUtils.KEY_NAME;
import static org.coodex.junit.enhance.TestUtils.KEY_TIMESTAMP;

class MapContextUtil {
    interface Annotated {
        <A extends Annotation> A getAnnotation(Class<A> aClass);
    }

    static Map<String, Object> buildMapContext(Annotated annotated) {
        MapContext mapContext = annotated.getAnnotation(MapContext.class);
        Map<String, Object> map = new HashMap<>();
        if (mapContext != null) {
            for (Entry entry : mapContext.value()) {
                map.put(entry.key(), entry.value());
            }
        }
        Entry entry = annotated.getAnnotation(Entry.class);

        if (entry != null)
            map.put(entry.key(), entry.value());
        return map;
    }

    static void appendContext(Map<String, Object> context, Annotated annotated, Supplier<String> defaultNameSupplier) {
        Context.Data contextData = Context.Data.from(annotated.getAnnotation(Context.class));
        context.put(KEY_NAME, Common.isBlank(contextData.name) ? defaultNameSupplier.get() : contextData.name);
        try {
            context.put(KEY_TIMESTAMP,
                    Common.isBlank(contextData.timestamp) ?
                            Clock.getCalendar() :
                            Common.strToCalendar(contextData.timestamp, Common.DEFAULT_DATETIME_FORMAT));
        } catch (Throwable th) {
            System.err.println("invalid timestamp: " + contextData.timestamp);
            context.put(KEY_TIMESTAMP, Clock.getCalendar());
        }

    }


}
