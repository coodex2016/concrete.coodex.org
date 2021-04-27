/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.test;

import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootApplication
@Import(ConcreteSpringConfiguration.class)
public class ProfileTest {

    private final static Logger log = LoggerFactory.getLogger(ProfileTest.class);


    public static void test1() {
        Profile p1 = Profile.get("a1");
        log.info("a1.t0: {}\ta1.t1: {}\ta1.t2: {}\ta1.t3: {}",
                p1.getInt("a1.t0"),
                p1.getInt("a1.t1"),
                p1.getInt("a1.t2"),
                p1.getInt("a1.t3"));
        p1 = Profile.get("b1");
        log.info("b1.t0: {}\tb1.t1: {}\tb1.t2: {}\tb1.t3: {}",
                p1.getInt("b1.t0"),
                p1.getInt("b1.t1"),
                p1.getInt("b1.t2"),
                p1.getInt("b1.t3"));
    }

    public static void main(String[] args) {
//        System.setProperty("spring.active.profiles", "t1,t2,t3");
        ApplicationContext context = SpringApplication.run(ProfileTest.class);
//        test1();
        System.out.println(context.getEnvironment().getProperty("test.org"));
//        System.out.println(context.getEnvironment().getProperty("test.array"));
//        System.out.println(context.getEnvironment().getProperty("test.array", List.class));
//        System.out.println(context.getEnvironment().containsProperty("test.array[0]"));
        for(int i = 0; context.getEnvironment().containsProperty("test.array[" + i + "]");i++){
            System.out.println(context.getEnvironment().getProperty("test.array[" + i + "]"));
        }
    }
}
