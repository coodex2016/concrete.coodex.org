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

package test.org.coodex.concrete.impl;

import org.coodex.commons.jpa.criteria.Operators.Logical;
import org.coodex.commons.jpa.springdata.SpecCommon;
import org.coodex.concrete.test.ConcreteTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.org.coodex.concrete.entities.TestEntity;
import test.org.coodex.concrete.repo.TestRepo;

import javax.inject.Inject;

/**
 * Created by davidoff shen on 2017-03-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test.xml")
public class TestCase extends ConcreteTestCase {

    @Inject
    private TestRepo testRepo;


    @Test
    public void testIntSpec() {
        // =
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.EQUAL, "intAttr", 1));

        // <
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.LESS, "intAttr", 1));

        // <=
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.LESS_EQUAL, "intAttr", 1));

        // >
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.GREATER, "intAttr", 1));

        // >=
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.GREATER_EQUAL, "intAttr", 1));

        // not
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.NOT_EQUAL, "intAttr", 1));

        // in
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.IN, "intAttr", 1));

        // between
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.BETWEEN, "intAttr", 1, 2));
    }

    @Test
    public void testStr() {
        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.EQUAL, "strAttr", "%"));

        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.LESS_EQUAL, "strAttr", "%"));

        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.IN, "strAttr", "%", "33"));

        testRepo.findAll(
                SpecCommon.spec(TestEntity.class, Logical.LIKE, "strAttr", "adsf"));

    }

    @Test
    public void testMemberOf() {
        Specification<TestEntity> specification = SpecCommon.memberOf(TestEntity.class, "colAttr", "x");
        testRepo.findAll(specification);
    }
}
