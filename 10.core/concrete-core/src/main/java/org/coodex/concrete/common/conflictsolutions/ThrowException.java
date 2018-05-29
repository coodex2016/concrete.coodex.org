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

package org.coodex.concrete.common.conflictsolutions;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConflictSolution;
import org.coodex.concrete.common.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public class ThrowException implements ConflictSolution {
    private final static Logger log = LoggerFactory.getLogger(ThrowException.class);

    @Override
    public boolean accept(Class clazz) {
        return false;
    }

    @Override
    public <T> T conflict(Map<String, T> beans, Class<T> clz) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("bean conflict for ").append(clz.getName()).append(":");
        for (String name : beans.keySet()) {
            stringBuilder.append("\n\t").append(name).append(", ").append(beans.get(name)).append(",")
                    .append(beans.get(name).getClass());
        }
        log.error(stringBuilder.toString());
        throw new ConcreteException(ErrorCodes.BEAN_CONFLICT, clz, beans.size());
    }
}
