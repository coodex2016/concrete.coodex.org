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

package org.coodex.concrete.core.mocker;

import org.coodex.concrete.api.mockers.ID;
import org.coodex.pojomocker.AbstractMocker;
import org.coodex.util.Common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by davidoff shen on 2017-05-15.
 */
@Deprecated
public class IDMocker extends AbstractMocker<ID> {

    private AtomicLong atomicLong = new AtomicLong(1);

    @Override
    public Object mock(ID mockAnnotation, Class clazz) {
        if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return atomicLong.incrementAndGet();
        } else if (String.class.equals(clazz)) {
            return Common.getUUIDStr();
        } else {
            return null;
        }
    }
}
