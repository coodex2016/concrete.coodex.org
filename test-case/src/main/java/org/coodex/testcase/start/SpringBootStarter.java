/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.testcase.start;


import org.coodex.concrete.amqp.AMQPConnectionConfig;
import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.coodex.concrete.support.amqp.AMQPApplication;
import org.coodex.testcase.api.TestCase;
import org.coodex.util.Profile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:testcase.xml")
@Import(ConcreteSpringConfiguration.class)
public class SpringBootStarter {

    @Bean
    public AMQPApplication getAMQPApplication() {
        AMQPConnectionConfig config = new AMQPConnectionConfig();
        Profile profile = Profile.getProfile("client.amqp.properties");
        config.setUri(profile.getString("location"));
        config.setUsername(profile.getString("amqp.username"));
        config.setPassword(profile.getString("amqp.password"));
        AMQPApplication amqpApplication = new AMQPApplication(
                config
        );
        amqpApplication.registerClasses(TestCase.class);
        return amqpApplication;
    }


    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarter.class, args);
    }

}
