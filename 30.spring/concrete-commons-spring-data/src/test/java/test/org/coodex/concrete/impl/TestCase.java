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
import org.coodex.commons.jpa.springdata.SpecCommon;
import org.coodex.concrete.test.ConcreteTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specifications;
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

    @Inject
    private AbstractSpecificationsMaker<Condition, TestEntity> abstractSpecificationsMaker;


    @Test
    public void testIntSpec() {
        // =
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>equals("intAttr", 1));

        // <
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>lessThen("intAttr", 1));

        // <=
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>lessThenEquals("intAttr", 1));

        // >
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>greaterThen("intAttr", 1));

        // >=
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>greaterThenEquals("intAttr", 1));

        // not
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>notEquals("intAttr", 1));

        // in
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>in("intAttr", 1));

        // between
        testRepo.findAll(
                SpecCommon.<TestEntity, Integer>between("intAttr", 1, 2));

    }

    @Test
    public void testStr() {
        testRepo.findAll(
                SpecCommon.<TestEntity, String>equals("strAttr", "%"));

        testRepo.findAll(
                SpecCommon.<TestEntity, String>lessThenEquals("strAttr", "%"));

        testRepo.findAll(
                SpecCommon.<TestEntity, String>in("strAttr", "%", "33"));

        testRepo.findAll(
                SpecCommon.<TestEntity>like("strAttr", "adsf"));

    }

    @Test
    public void testMemberOf() {
//        Specification<TestEntity> specification = SpecCommon.<TestEntity, String>memberOf("colAttr", "x");
        testRepo.findAll(SpecCommon.<TestEntity, String>memberOf("colAttr", "x"));
    }

    @Test
    public void testNull() {
        testRepo.findAll((Specifications<TestEntity>) null, new PageRequest(1, 1));
    }

    @Test
    public void testMaker() {
        testRepo.findAll(abstractSpecificationsMaker.make(null, "aaa"));
        testRepo.findAll(abstractSpecificationsMaker.make(null, "a01"));
    }
}
