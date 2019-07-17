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

package org.coodex.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.coodex.util.TypeHelper.solve;

/**
 * Created by davidoff shen on 2017-05-11.
 * @deprecated 20190716, 使用 {@link GenericTypeHelper}的内部类替代，0.3.2移除
 */
@Deprecated
public abstract class GenericType<T> {


    public final Type genericType() {
        return genericType(null);
    }

    public final Type genericType(Class context) {
        return solve(GenericType.class.getTypeParameters()[0], getClass(), context);
    }

    public final Type getType() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }


}
