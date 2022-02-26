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
import org.coodex.util.ServiceLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.org.coodex.bean.processors.a.TestIntf;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(ConcreteSpringConfiguration.class)
@ContextConfiguration(classes = ServiceLoaderBeanTest.class)
@Configuration
public class ServiceLoaderBeanTest {

    @Inject
    private ServiceLoader<TestIntf> test1;

    @Inject
//    @DefaultService
    private ServiceLoader<TestIntf> test2;

    private ServiceLoader<TestIntf> test3;//null;

    @Inject
//    @DefaultService(TestIntfImplWithQualifier.class)
    private ServiceLoader<TestIntf> test4;


    @Test
    public void test() {
        System.out.println(test1);
        Assert.assertNull(test1.get());
        Assert.assertNull(test3);
        TestIntf testInft = test2.get();
        Assert.assertThrows(RuntimeException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                testInft.x();
            }
        });
        System.out.println(test4.get().x());
    }

}
