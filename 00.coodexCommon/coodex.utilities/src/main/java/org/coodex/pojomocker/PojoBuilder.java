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

package org.coodex.pojomocker;

import org.coodex.util.PojoInfo;
import org.coodex.util.PojoProperty;

/**
 * Created by davidoff shen on 2017-05-13.
 */
@Deprecated
public interface PojoBuilder {

    Object newInstance(PojoInfo pojoInfo) throws Throwable;

    void set(Object instance, PojoProperty property, Object value) throws Throwable;

    Object get(Object instance, PojoProperty property) throws Throwable;

}
