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
import org.coodex.servlet.cors.CorsFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;

/**
 * Created by davidoff shen on 2017-03-29.
 */
@SpringBootApplication
@Configuration
@ImportResource({"classpath:example.xml"})
public class Starter extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Starter.class);
    }

    @Bean
    public ServletRegistrationBean testServlet() {
        ServletContainer container = new ServletContainer();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                container, "/*");
//        registrationBean.ad
        registrationBean.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                ExampleApplication.class.getName());
        registrationBean.setName("test");
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new CorsFilter());
        filterRegistrationBean.setAsyncSupported(true);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
        return filterRegistrationBean;
    }



    public static void main(String[] args) {
        SpringApplication.run(new Object[]{
                Starter.class
        }, args);
    }
}
