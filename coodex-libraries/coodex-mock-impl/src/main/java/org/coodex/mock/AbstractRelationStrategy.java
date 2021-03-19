/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.mock;

import org.coodex.util.Common;
import org.coodex.util.Singleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRelationStrategy implements RelationStrategy {

    //    private String[] strategies = Singleton<;
    private Singleton<String[]> strategies = Singleton.with(() -> {
        Class<?> c = AbstractRelationStrategy.this.getClass();
        List<String> list = new ArrayList<>();
        for (Method method : c.getMethods()) {
            Strategy strategy = method.getAnnotation(Strategy.class);
            if (strategy != null) {
                list.add(strategy.value());
            }
        }
        return list.toArray(new String[0]);
    });

    @Override
    public final boolean accept(String strategyName) {
//        if (strategies == null) {
//            synchronized (this) {
//                if (strategies == null) {
//                    Class<?> c = getClass();
//                    List<String> list = new ArrayList<>();
//                    for (Method method : c.getMethods()) {
//                        Strategy strategy = method.getAnnotation(Strategy.class);
//                        if (strategy != null) {
//                            list.add(strategy.value());
//                        }
//                    }
//                    strategies = list.toArray(new String[0]);
//                }
//            }
//        }
        return Common.inArray(strategyName, strategies.get());
    }
}
