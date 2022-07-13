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
import org.coodex.concrete.support.amqp.AMQPApplication;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class ConcreteAMQPBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    private static final String BEAN_NAME = "concreteAMQPApplication";
    private static final AMQPRuntime runtime = new AMQPRuntime();

    public ConcreteAMQPBeanDefinitionRegistrar() {
        ConcreteHelper.printBanner("  _____                      __        ___   __  _______    ___ \n" +
                        " / ___/__  ___  ___________ / /____   / _ | /  |/  / __ \\  / _ \\\n" +
                        "/ /__/ _ \\/ _ \\/ __/ __/ -_) __/ -_) / __ |/ /|_/ / /_/ / / ___/\n" +
                        "\\___/\\___/_//_/\\__/_/  \\__/\\__/\\__/ /_/ |_/_/  /_/\\___\\_\\/_/    \n",
                "Concrete AMQP", true);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (registry) { // NOSONAR
                if (!registry.containsBeanDefinition(BEAN_NAME)) {
                    AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                            importingClassMetadata.getAnnotationAttributes(
                                    EnableConcreteAMQP.class.getName(),
                                    false
                            )
                    );

                    if (annotationAttributes != null) {
                        runtime.loadFrom(annotationAttributes);
                    }

                    RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(ConcreteAMQPApplication.class);
//                    rootBeanDefinition.setDependsOn(ConcreteSpringConfigurationBeanDefinitionRegistrar.CONFIGURATION_BEAN_NAME);
                    registry.registerBeanDefinition(BEAN_NAME, rootBeanDefinition);
                }
            }
        }
    }

    public static class ConcreteAMQPApplication extends AMQPApplication {

        public ConcreteAMQPApplication() {
            super(runtime.getConfig(), runtime.getExchangeName(), runtime.getQueueName(), runtime.getTtl());
            registerClasses(runtime.getClasses());
            registerPackage(runtime.getApiPackages());
        }
    }
}
