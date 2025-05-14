/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.support.dubbo.ApacheDubboApplication;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class ConcreteDubboBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String BEAN_NAME = "concreteDubboApplication";
    private static final DubboRuntime runtime = new DubboRuntime();

    public ConcreteDubboBeanDefinitionRegistrar() {
        ConcreteHelper.printBanner("  _____                      __        ___       __   __      \n" +
                        " / ___/__  ___  ___________ / /____   / _ \\__ __/ /  / /  ___ \n" +
                        "/ /__/ _ \\/ _ \\/ __/ __/ -_) __/ -_) / // / // / _ \\/ _ \\/ _ \\\n" +
                        "\\___/\\___/_//_/\\__/_/  \\__/\\__/\\__/ /____/\\_,_/_.__/_.__/\\___/\n",
                "Concrete Dubbo", true);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        if (!beanDefinitionRegistry.containsBeanDefinition(BEAN_NAME)) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (beanDefinitionRegistry) {// NOSONAR
                if (!beanDefinitionRegistry.containsBeanDefinition(BEAN_NAME)) {
                    AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                            annotationMetadata.getAnnotationAttributes(
                                    EnableConcreteApacheDubbo.class.getName(),
                                    false
                            )
                    );

                    if (annotationAttributes != null) {
                        runtime.loadFrom(annotationAttributes);
                    }

                    if (runtime.getRegistries() == null || runtime.getRegistries().length == 0) {
                        // 默认起一个simple注册表服务

                        runtime.setRegistries(new String[]{"double"});

                    }

                    RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(ConcreteDubboApplication.class);
//                    rootBeanDefinition.setDependsOn(ConcreteSpringConfigurationBeanDefinitionRegistrar.CONFIGURATION_BEAN_NAME);
                    beanDefinitionRegistry.registerBeanDefinition(BEAN_NAME, rootBeanDefinition);
                }
            }
        }
    }

    public static class ConcreteDubboApplication extends ApacheDubboApplication {
        public ConcreteDubboApplication() {
            super(runtime.getName(), runtime.getRegistries(), runtime.getProtocols(), runtime.getVersion());
            register(runtime.getClasses());
            registerPackage(runtime.getApiPackages());
        }
    }
}
