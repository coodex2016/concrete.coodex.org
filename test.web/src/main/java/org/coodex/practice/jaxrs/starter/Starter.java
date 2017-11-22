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

import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.coodex.concrete.support.websocket.CallerHackFilter;
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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import java.util.Arrays;

/**
 * Created by davidoff shen on 2017-03-29.
 */
@SpringBootApplication
@Configuration
@Import(ConcreteSpringConfiguration.class)
@ImportResource({"classpath:example.xml"})
public class Starter extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Starter.class);
    }

    @Bean
    public ServletContainer getServletContainer(){
        return new ServletContainer();
    }

    @Bean
    public ServletRegistrationBean testServlet() {

        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                getServletContainer(), "/jaxrs/*");
        registrationBean.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                ExampleApplication.class.getName());
        registrationBean.setName("test");
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean wsFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new CallerHackFilter());
        filterRegistrationBean.setAsyncSupported(true);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/WebSocket"));
        return filterRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean wsServlet() {
//        ServletContainer container = new ServletContainer();

        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                getServletContainer(), "/WebSocket")
        {
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.addListener(new ServletContextListener() {

                    @Override
                    public void contextInitialized(ServletContextEvent sce) {
                        final ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
                                .getAttribute("javax.websocket.server.ServerContainer");

                        try {
                            serverContainer.addEndpoint(TestWebSocketServer.class);
                        } catch (DeploymentException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void contextDestroyed(ServletContextEvent sce) {

                    }
                });
            }
        }
                ;

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
