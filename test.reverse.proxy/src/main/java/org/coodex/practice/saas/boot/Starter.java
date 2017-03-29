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

package org.coodex.practice.saas.boot;


import org.coodex.practice.saas.jersey.JerseyResource;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Created by davidoff shen on 2017-03-29.
 */

@SpringBootApplication

public class Starter implements EmbeddedServletContainerCustomizer {

    @Bean
    public ServletRegistrationBean proxyServlet() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                new ServletContainer(), "/*");
        registrationBean.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                JerseyResource.class.getName());
        registrationBean.setName("proxy");
        return registrationBean;
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(8081);
    }


    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }


}
