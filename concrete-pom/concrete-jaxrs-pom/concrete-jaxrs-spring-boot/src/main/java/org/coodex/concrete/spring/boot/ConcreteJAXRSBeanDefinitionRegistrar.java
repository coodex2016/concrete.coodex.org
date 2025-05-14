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

import org.coodex.concrete.common.ConcreteHelper;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

public class ConcreteJAXRSBeanDefinitionRegistrar
        implements ImportBeanDefinitionRegistrar {

    private static final String BEAN_NAME = "concreteJaxrsServletRegistrationBean";

    private static final JaxrsRuntime runtime = new JaxrsRuntime();

    static String[] getApiPackages() {
        return runtime.getApiPackages();
    }

    static Class<?>[] getClasses() {
        return runtime.getClasses();
    }

    public ConcreteJAXRSBeanDefinitionRegistrar() {
        printBanner();
    }

    public static void printBanner() {
        ConcreteHelper.printBanner("  _____                      __           _____   _  _____  ____\n" +
                        " / ___/__  ___  ___________ / /____   __ / / _ | | |/_/ _ \\/ __/\n" +
                        "/ /__/ _ \\/ _ \\/ __/ __/ -_) __/ -_) / // / __ |_>  </ , _/\\ \\  \n" +
                        "\\___/\\___/_//_/\\__/_/  \\__/\\__/\\__/  \\___/_/ |_/_/|_/_/|_/___/  \n",
                "Concrete JAXRS", true);
    }


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (registry) {// NOSONAR
                if (!registry.containsBeanDefinition(BEAN_NAME)) {
                    AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                            importingClassMetadata.getAnnotationAttributes(
                                    EnableConcreteJAXRS.class.getName(),
                                    false
                            )
                    );

                    if (annotationAttributes != null) {
                        runtime.loadFrom(annotationAttributes);
                    }

                    RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(
                            ConcreteJAXRSServletRegistrationBean.class
                    );
//                    rootBeanDefinition.setDependsOn(ConcreteSpringConfigurationBeanDefinitionRegistrar.CONFIGURATION_BEAN_NAME);
                    registry.registerBeanDefinition(BEAN_NAME, rootBeanDefinition);
                }
            }
        }
    }

    public static class ConcreteJAXRSServletRegistrationBean
            extends ServletRegistrationBean<ServletContainer> {

        public ConcreteJAXRSServletRegistrationBean() {
            super(new ServletContainer());
            setUrlMappings(Arrays.asList(runtime.getUrlMappings()));
            addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, runtime.getApplicationClassName());
            setAsyncSupported(true);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++)
            printBanner();
    }
}
