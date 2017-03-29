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

package org.coodex.practice.jaxrs.starter;

import org.coodex.practice.jaxrs.jersey.ExampleApplication;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by davidoff shen on 2017-03-29.
 */
@SpringBootApplication
@Configuration
@ImportResource({"classpath:example.xml"})
public class Starter {
    @Bean
    public ServletRegistrationBean testServlet() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                new ServletContainer(), "/*");
        registrationBean.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                ExampleApplication.class.getName());
        registrationBean.setName("test");
        return registrationBean;
    }

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}
