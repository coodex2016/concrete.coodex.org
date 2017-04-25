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

package org.coodex.concrete.common;

import java.util.*;

import static org.coodex.concrete.common.ConcreteContext.SUBJOIN;
import static org.coodex.util.Common.concat;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public class SubjoinWrapper implements Subjoin {

    public static final Subjoin DEFAULT_SUBJOIN = new Subjoin() {

        private Map<String, List<String>> subjoin = new HashMap<String, List<String>>();

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }

        @Override
        public String get(String name) {
            return get(name, "; ");
        }

        @Override
        public String get(String name, String split) {
            return concat(subjoin.get(name), split);
        }


        @Override
        public List<String> getList(String name) {
            return subjoin.get(name);
        }

        @Override
        public Set<String> keySet() {
            return subjoin.keySet();
        }

        @Override
        public void set(String name, List<String> values) {
            subjoin.put(name, values);
        }

        @Override
        public void add(String name, String value) {
            List<String> list = getList(name);
            if (list == null) {
                list = new ArrayList<String>();
                subjoin.put(name, list);
            }
            list.add(value);
        }
    };

    public static Subjoin getInstance() {
        return SUBJOIN.get() == null ? DEFAULT_SUBJOIN : SUBJOIN.get();
    }

    @Override
    public Locale getLocale() {
        return getInstance().getLocale();
    }

    @Override
    public String get(String name) {
        return getInstance().get(name);
    }

    @Override
    public String get(String name, String split) {
        return getInstance().get(name, split);
    }

    @Override
    public List<String> getList(String name) {
        return getInstance().getList(name);
    }

    @Override
    public Set<String> keySet() {
        return getInstance().keySet();
    }

    @Override
    public void set(String name, List<String> values) {
        getInstance().set(name, values);
    }

    @Override
    public void add(String name, String value) {
        getInstance().add(name, value);
    }
}
