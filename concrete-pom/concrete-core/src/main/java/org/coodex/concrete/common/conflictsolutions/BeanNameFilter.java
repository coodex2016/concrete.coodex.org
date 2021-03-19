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

import org.coodex.config.Config;
import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * 基于Bean的名字过滤出唯一的实现
 * Created by davidoff shen on 2016-11-01.
 */
@SuppressWarnings("unused")
public class BeanNameFilter extends AbstractConflictSolution /*implements ConflictSolution*/ {

//    private static final Profile_Deprecated profile = ConcreteHelper.getProfile();


    @Override
    public boolean accept(Class<?> clazz) {
        return true;
    }

    @Override
    protected <T> Map<String, T> doFilter(Map<String, T> beans, Class<T> clz) {

        String prefix = Config.get(BeanNameFilter.class.getCanonicalName() + ".prefix", getAppSet());
        if (Common.isBlank(prefix)) return beans;

        Map<String, T> map = new HashMap<>();
        for (String str : beans.keySet()) {
            if (str != null && str.startsWith(prefix))
                map.put(str, beans.get(str));
        }
        return map;
    }

//    private Set<String> filter(Set<String> set) {
//        String prefix = Config.get(BeanNameFilter.class.getCanonicalName() + ".prefix", getAppSet());
//
//        if (Common.isBlank(prefix)) return set;
//
//        Set<String> stringSet = new HashSet<String>();
//        for (String str : set) {
//            if (str != null && str.startsWith(prefix))
//                stringSet.add(str);
//        }
//        return stringSet;
//    }

//    @Override
//    public <T> T conflict(Map<String, T> beans, Class<T> clz) {
//        Set<String> set = filter(beans.keySet());
//        switch (set.size()) {
//            case 0:
//                throw new ConcreteException(ErrorCodes.NO_SERVICE_INSTANCE_FOUND, clz);
//            case 1:
//                return beans.get(set.iterator().next());
//            default:
//                throw new ConcreteException(ErrorCodes.BEAN_CONFLICT, clz, set.size());
//        }
//    }
}
