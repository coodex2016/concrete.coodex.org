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

package org.coodex.concrete.common;


import org.coodex.util.Common;
import org.coodex.util.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractChangeableSubjoin extends AbstractSubjoin {

    private Singleton<Subjoin> subjoinSingleton = new Singleton<Subjoin>(
            new Singleton.Builder<Subjoin>() {
                @Override
                public Subjoin build() {
                    return new WrappedSubjoin(AbstractChangeableSubjoin.this);
                }
            }
    );

    /**
     * @return 包装后的Subjoin，会记录下自wrap之后所有的set信息，方便数据回传
     */
    public Subjoin wrap() {
        return subjoinSingleton.getInstance();
    }

    private static class WrappedSubjoin extends AbstractSubjoin {
        private final Subjoin subjoin;

        public WrappedSubjoin(Subjoin subjoin) {
            this.subjoin = subjoin;
        }

        @Override
        protected Collection<String> skipKeys() {
            return null;
        }

        @Override
        public String get(String name, String split) {
            return containsKey(name) ? super.get(name, split) : subjoin.get(name, split);
        }

        @Override
        public List<String> getList(String name) {
            return containsKey(name) ? super.getList(name) : subjoin.getList(name);
        }

        public Set<String> keySet() {
            return Common.join(subjoin.keySet(), super.keySet());
        }

        @Override
        public void set(String name, List<String> values) {
            super.set(name, values);
        }

        @Override
        public void add(String name, String value) {
            List<String> list = getList(name);
            if (list == null)
                list = new ArrayList<String>();

            if (!list.contains(value)) {
                list.add(value);
            }
            set(name, list);
        }

    }

}
