///*
// * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.coodex.concrete.spring;
//
//import org.coodex.util.UUIDHelper;
//import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.beans.factory.support.RootBeanDefinition;
//import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
//import org.springframework.core.type.AnnotationMetadata;
//
//@Deprecated
//public class ConcreteSpringConfigurationBeanDefinitionRegistrar
//        implements ImportBeanDefinitionRegistrar {
//
//    public static final String CONFIGURATION_BEAN_NAME = UUIDHelper.getUUIDString();
//
//    @Override
//    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
//        if (!registry.containsBeanDefinition(CONFIGURATION_BEAN_NAME)) {
//            //noinspection SynchronizationOnLocalVariableOrMethodParameter
//            synchronized (registry) {
//                if (!registry.containsBeanDefinition(CONFIGURATION_BEAN_NAME)) {
//                    RootBeanDefinition beanDefinition = new RootBeanDefinition(ConcreteSpringConfiguration.class);
//                    registry.registerBeanDefinition(CONFIGURATION_BEAN_NAME, beanDefinition);
//                }
//            }
//        }
//
//    }
//
//}
