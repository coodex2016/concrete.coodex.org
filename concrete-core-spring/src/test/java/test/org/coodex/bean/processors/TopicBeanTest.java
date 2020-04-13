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

import org.coodex.concrete.message.Queue;
import org.coodex.concrete.message.Topic;
import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.Serializable;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(ConcreteSpringConfiguration.class)
@ContextConfiguration(classes = TopicBeanTest.class)
//@ComponentScan("test.org.coodex.bean.processors.a")
@Configuration
public class TopicBeanTest {

//    @Bean
//    public TestIntf a(){
//        return new TestIntfImplNoQualufier();
//    }
//
//    @Bean
//    public TestIntf b(){
//        return new TestIntfImplWithQualifier();
//    }
//
//    @Inject
//    TestIntf noQualifier;
//
//    @Inject
//    @TestQualifier
//    TestIntf withQualifier;


    @Inject
    private Topic<String> stringTopic1;

    @Inject
    private Topic<String> stringTopic2;

    private Topic<String> stringTopic3;

    @Inject
    @Queue("test")
    private Topic<String> stringTopic4;

    @Inject
    @Queue("test")
    private Topic<String> stringTopic5;

    @Queue("test")
    private Topic<String> stringTopic6;

    @Test
    public void test() {

        System.out.println(stringTopic1);
        System.out.println(stringTopic2);
        System.out.println(stringTopic3);
        System.out.println(stringTopic4);
        System.out.println(stringTopic5);
        System.out.println(stringTopic6);

        Assert.assertEquals(stringTopic1, stringTopic2);
//        Assert.assertEquals(stringTopic1, stringTopic3);
        Assert.assertEquals(stringTopic4, stringTopic5);
//        Assert.assertEquals(stringTopic4, stringTopic6);
        Assert.assertNotEquals(stringTopic1, stringTopic4);

        stringTopic1.publish("11111");
    }

    static class XX implements Serializable {
    }

}
