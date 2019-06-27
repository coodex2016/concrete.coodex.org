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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public class SubjoinWrapper implements Subjoin {

    private static Subjoin instance = new SubjoinWrapper();


    public static Subjoin getInstance() {
        return instance;
    }

    private Subjoin getSubjoin() {
        ServiceContext serviceContext = getServiceContext();
        Subjoin subjoin = null;
        if (serviceContext != null) {
            subjoin = serviceContext.getSubjoin();
        }
        return subjoin == null || subjoin == this ? new DefaultSubjoin() : subjoin;
    }

    @Override
    public String get(String name) {
        return getSubjoin().get(name);
    }

    @Override
    public String get(String name, String split) {
        return getSubjoin().get(name, split);
    }

    @Override
    public List<String> getList(String name) {
        return getSubjoin().getList(name);
    }

    @Override
    public Set<String> keySet() {
        return getSubjoin().keySet();
    }

    @Override
    public Set<String> updatedKeySet() {
        return getSubjoin().updatedKeySet();
    }

    @Override
    public void set(String name, List<String> values) {
        getSubjoin().set(name, values);
    }

    @Override
    public void add(String name, String value) {
        getSubjoin().add(name, value);
    }

//    @Override
//    public List<Warning> getWarnings() {
//        return getSubjoin().getWarnings();
//    }

    @Override
    public void clearWarning() {
        getSubjoin().clearWarning();
    }

    @Override
    public void setWarnings(Collection<Warning> warings) {
        getSubjoin().setWarnings(warings);
    }

    @Override
    public void addAll(Collection<Warning> warnings) {
        getSubjoin().addAll(warnings);
    }

    @Override
    public void putWarning(Warning warning) {
        getSubjoin().putWarning(warning);
    }

    public static class DefaultSubjoin extends AbstractSubjoin {
        @Override
        protected Collection<String> skipKeys() {
            return null;
        }
    }
}
