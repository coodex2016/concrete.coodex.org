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
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ConflictSolution;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于Bean的名字过滤出唯一的实现
 * Created by davidoff shen on 2016-11-01.
 */
public class BeanNameFilter implements ConflictSolution {

    private static final Profile profile = ConcreteHelper.getProfile();

    private Set<String> filter(Set<String> set) {
        String prefix = profile.getString(BeanNameFilter.class.getCanonicalName() + ".prefix");
        if (Common.isBlank(prefix)) return set;

        Set<String> stringSet = new HashSet<String>();
        for (String str : set) {
            if (str != null && str.startsWith(prefix))
                stringSet.add(str);
        }
        return stringSet;
    }

    @Override
    public boolean accepted(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T conflict(Map<String, T> beans, Class<T> clz) {
        Set<String> set = filter(beans.keySet());
        switch (set.size()) {
            case 0:
                throw new ConcreteException(ErrorCodes.NO_SERVICE_INSTANCE_FOUND, clz);
            case 1:
                return beans.get(set.iterator().next());
            default:
                throw new ConcreteException(ErrorCodes.BEAN_CONFLICT, clz, set.size());
        }
    }
}
