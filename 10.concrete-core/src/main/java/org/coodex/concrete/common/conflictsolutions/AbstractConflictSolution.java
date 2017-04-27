/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.common.conflictsolutions;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConflictSolution;
import org.coodex.concrete.common.ErrorCodes;

import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-27.
 */
public abstract class AbstractConflictSolution implements ConflictSolution {
    @Override
    public <T> T conflict(Map<String, T> beans, Class<T> clz) {
        Map<String, T> map = doFilter(beans, clz);
        if (map == null || map.size() == 0) {
            throw new ConcreteException(ErrorCodes.NO_SERVICE_INSTANCE_FOUND, clz);
        } else if (map.size() == 1) {
            return map.values().iterator().next();
        } else {
            throw new ConcreteException(ErrorCodes.BEAN_CONFLICT, clz, map.size());
        }
    }

    abstract protected <T> Map<String, T> doFilter(Map<String, T> beans, Class<T> clz);
}
