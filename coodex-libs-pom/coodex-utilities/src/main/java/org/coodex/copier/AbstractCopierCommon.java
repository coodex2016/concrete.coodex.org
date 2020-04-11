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

package org.coodex.copier;

import org.coodex.util.Common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.TypeVariable;
import java.util.*;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;
import static org.coodex.util.GenericTypeHelper.typeToClass;


/**
 * Created by davidoff shen on 2017-05-11.
 */

public abstract class AbstractCopierCommon {

    private Class<?>[] classes = new Class<?>[2];

    protected Class<?> getClass(Index index) {
        synchronized (this) {
            if (classes[index.getIndex()] == null) {
                TypeVariable<?> t = AbstractCopierCommon.class.getTypeParameters()[index.getIndex()];
                Class<?> clz = typeToClass(solveFromInstance(t, this));
                if (clz == null) throw new RuntimeException("unknown class: " + t);
                classes[index.getIndex()] = clz;
            }
        }
        return classes[index.getIndex()];
    }

    protected Object newObject(Index index) {
        try {
            return getClass(index).newInstance();
        } catch (Throwable th) {
            throw Common.rte(th);
        }
    }

    protected Object init(Object o, Index index) {
        if (o == null)
            o = newObject(index);
        return o;
    }

    protected abstract Object copy(Object o, Index srcIndex);


    protected <T extends Collection<?>> T copy(Collection<?> srcCollection, Class<T> tClass, Index srcIndex) {
        if (srcCollection == null) throw new NullPointerException("srcCollection is NULL.");
        Collection<?> collection = null;
        if (List.class.equals(tClass)) {
            collection = new ArrayList<>();
        } else if (Set.class.equals(tClass)) {
            collection = new HashSet<>();
        } else {
            try {
                collection = tClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        }
        if (collection == null)
            throw new IllegalArgumentException("class :" + tClass.getCanonicalName() + " not support.");
        for (Object src : srcCollection) {

            collection.add(Common.cast(copy(src, srcIndex)));
        }
        return Common.cast(collection);
    }

    private Class<? extends Collection<?>> getCollectionClass(Collection<?> collection) {
        Class<?> clazz = collection.getClass();
        if (List.class.isAssignableFrom(clazz)) {
            return Common.cast(List.class);
        } else if (Set.class.isAssignableFrom(clazz)) {
            return Common.cast(Set.class);
        } else
            return Common.cast(clazz);
    }

    protected <T extends Collection<?>> T copy(Collection<?> srcCollection, Index srcIndex) {
        return Common.cast(copy(srcCollection, getCollectionClass(srcCollection), srcIndex));
    }

    protected enum Index {
        A(0), B(1);

        private final int index;

        Index(int i) {
            index = i;
        }

        int getIndex() {
            return index;
        }
    }


}
