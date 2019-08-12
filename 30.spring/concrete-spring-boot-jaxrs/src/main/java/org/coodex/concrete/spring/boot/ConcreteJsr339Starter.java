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

package org.coodex.concrete.spring.boot;

import org.coodex.concrete.support.jsr339.ConcreteJSR339Application;
import org.coodex.config.Config;
import org.coodex.util.Singleton;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

@Configuration
@SpringBootApplication
public class ConcreteJsr339Starter {

    private Singleton<Servlet> servletSingleton = new Singleton<>(ServletContainer::new);

    public static void main(String[] args) {
        SpringApplication.run(ConcreteJsr339Starter.class, args);
    }

    protected String getContextPath() {
        return Config.getValue("concrete.jaxrs.servletMapping", "/jaxrs", "concrete", "jaxrs", getAppSet());
    }

    protected Class<? extends ConcreteJSR339Application> getJSR339ApplicationClass() {
        return Jsr339Application.class;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        //noinspection unchecked
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                servletSingleton.get(), getContextPath() + "/*");
        // 使用jersey发布jaxrs应用，concrete在javax.rs.Application基础上进行了封装
        registrationBean.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                getJSR339ApplicationClass().getName());
        // servlet3.0带来的新特性，异步servlet，极大提高了servlet处理能力，强烈推荐
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

}
