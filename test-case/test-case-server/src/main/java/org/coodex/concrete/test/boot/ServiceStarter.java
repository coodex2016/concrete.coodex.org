/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.test.boot;

import org.coodex.concrete.core.token.TokenManager;
import org.coodex.concrete.core.token.local.LocalTokenManager;
import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.coodex.concrete.support.dubbo.DubboApplication;
import org.coodex.concrete.support.jsr339.ConcreteJaxrs339Application;
import org.coodex.concrete.support.websocket.CallerHackConfigurator;
import org.coodex.concrete.support.websocket.ConcreteWebSocketApplication;
import org.coodex.concrete.test.api.Test;
import org.coodex.concrete.test.impl.TestImpl;
import org.coodex.util.Profile;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;

import static org.coodex.concrete.dubbo.DubboHelper.buildRegistryConfigs;

@SpringBootApplication
@Configuration
@EnableAspectJAutoProxy
@Import(ConcreteSpringConfiguration.class)
public class ServiceStarter {

    private static Profile profile = Profile.getProfile("env.properties");

    public static void main(String[] args) {
        SpringApplication.run(ServiceStarter.class);
    }

//    @Bean
//    public TestTopic getTestTopic(){
//        return new TestTopic();
//    }

    @Bean
    public ServletRegistrationBean jaxrsServlet() {
        ServletContainer container = new ServletContainer();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                container, "/jaxrs/*");
        registrationBean.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                JaxRSApplication.class.getName());
        registrationBean.setName("demo");
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean webSocketServlet() {
        ServletContainer container = new ServletContainer();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                container, "/WebSocket") {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.addListener(new ServletContextListener() {

                    @Override
                    public void contextInitialized(ServletContextEvent sce) {
                        final ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
                                .getAttribute("javax.websocket.server.ServerContainer");

                        try {
                            serverContainer.addEndpoint(WebsocketApplication.class);
                        } catch (DeploymentException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void contextDestroyed(ServletContextEvent sce) {

                    }
                });
            }
        };
        registrationBean.setName("demo");
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    public DubboApplication dubboApplication() {
        DubboApplication dubboApplication = new DubboApplication(
                "test", buildRegistryConfigs(new String[]{profile.getString("registry")})
        );
        dubboApplication.register(Test.class);
        return dubboApplication;
    }

    @Bean
    public TokenManager tokenManager() {
        return new LocalTokenManager();
    }

    @Bean
    public Test getTest() {
        return new TestImpl();
    }

    public static class JaxRSApplication extends ConcreteJaxrs339Application {
        public JaxRSApplication() {
            register(JacksonFeature.class,
                    LoggingFeature.class,
                    Test.class);
        }
    }

    @ServerEndpoint(value = "/WebSocket", configurator = CallerHackConfigurator.class)
    public static class WebsocketApplication extends ConcreteWebSocketApplication {
        public WebsocketApplication() {
            super();
            register(Test.class);
        }
    }


}
