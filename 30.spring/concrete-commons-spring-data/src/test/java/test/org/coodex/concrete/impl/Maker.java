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

package test.org.coodex.concrete.impl;

import org.coodex.commons.jpa.springdata.AbstractSpecificationsMaker;
import org.coodex.commons.jpa.springdata.MakerFunction;
import org.springframework.data.jpa.domain.Specification;
import test.org.coodex.concrete.entities.TestEntity;

public class Maker extends AbstractSpecificationsMaker<Condition, TestEntity> {

    public Specification<TestEntity> aaa(Condition condition) {
        return null;
    }

    @MakerFunction("a01")
    public Specification<TestEntity> bbb(Condition condition) {
        return null;
    }

}
