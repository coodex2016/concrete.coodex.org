/*
 * Copyright (c) 2016 - 2025 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import org.coodex.functional.Supplier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RenderServiceHelper {

    private static Object getObject(Object object) {
        if (object == null) return null;
//
        if (object instanceof Supplier) return ((Supplier<?>) object).get();
        // java 8支持
        Class<?> clazz = object.getClass();
        if (clazz.getName().equals("java.util.function.Supplier")) {
            try {
                Method method = clazz.getMethod("get");
                method.setAccessible(true);
                return method.invoke(object);
            } catch (NoSuchMethodException ignored) {

            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return object;
//        if(clazz.equals(Supplier.class)) return ()object.get();
//        o instanceof Supplier ? ((Supplier<?>) o).get() : o
    }

    public static Object[] transfer(Object... objects) {
        if (objects == null) {
            return null;
        }
        if (objects.length == 0) {
            return objects;
        }
        Object[] result = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];

            result[i] = getObject(o);
        }
        return result;
    }
}
