/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.bean.processors;

import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.coodex.util.DefaultService;
import org.coodex.util.SelectableServiceLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.org.coodex.bean.processors.b.NumberSelectableService;
import test.org.coodex.bean.processors.b.SelectableServiceImpl;
import test.org.coodex.bean.processors.b.SelectableTest;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(ConcreteSpringConfiguration.class)
@ContextConfiguration(classes = SelectableServiceLoaderTest.class)
@ComponentScan("test.org.coodex.bean.processors.b")
@Configuration
public class SelectableServiceLoaderTest {

    @Inject
    private SelectableServiceLoader<String, SelectableTest> serviceLoader1;

    @Inject
    @DefaultService// exception
    private SelectableServiceLoader<String, SelectableTest> serviceLoader2;

    @Inject
    @DefaultService(SelectableServiceImpl.class)
    private SelectableServiceLoader<String, SelectableTest> serviceLoader3;

    private SelectableServiceLoader<String, SelectableTest> serviceLoader4;

    @Inject
    private SelectableServiceLoader<Integer, NumberSelectableService> serviceLoader5;

    @Inject
    @DefaultService(NumberSelectableService.OddNumberSelectableService.class)
    private SelectableServiceLoader<Integer, NumberSelectableService> serviceLoader6;


    @Test
    public void test() {
        Assert.assertNotNull(serviceLoader1);
        Assert.assertNull(serviceLoader1.select("1"));
        Assert.assertNotNull(serviceLoader2);
        System.out.println(serviceLoader2.select("1"));
        Assert.assertNotNull(serviceLoader2.select("1"));
        Assert.assertThrows(RuntimeException.class, () -> serviceLoader2.select("1").hello());
        Assert.assertNotNull(serviceLoader3);
        Assert.assertNotNull(serviceLoader3.select("1"));
        Assert.assertEquals(serviceLoader3.select("1").hello(), "hello");
        Assert.assertNull(serviceLoader4);

        Assert.assertNotNull(serviceLoader5.select(2));
        Assert.assertNull(serviceLoader5.select(1));

        Assert.assertNotNull(serviceLoader6.select(2));
        Assert.assertNotNull(serviceLoader6.select(1));
    }

}
