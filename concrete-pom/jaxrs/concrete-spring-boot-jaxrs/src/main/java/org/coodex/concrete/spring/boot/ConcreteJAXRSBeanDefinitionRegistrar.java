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

import org.coodex.concrete.spring.ConcreteSpringConfigurationBeanDefinitionRegistrar;
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

    private static JaxrsRuntime runtime = new JaxrsRuntime();

    static String[] getApiPackages() {
        return runtime.getApiPackages();
    }

    static Class[] getClasses() {
        return runtime.getClasses();
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (registry) {
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
                    rootBeanDefinition.setDependsOn(ConcreteSpringConfigurationBeanDefinitionRegistrar.CONFIGURATION_BEAN_NAME);
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
}
