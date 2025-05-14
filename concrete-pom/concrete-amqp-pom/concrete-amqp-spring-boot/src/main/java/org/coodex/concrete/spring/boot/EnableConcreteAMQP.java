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

import org.coodex.concrete.spring.ConcreteSpringConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
//        ConcreteSpringConfigurationBeanDefinitionRegistrar.class,
        ConcreteSpringConfiguration.class,
        ConcreteAMQPBeanDefinitionRegistrar.class
})
@Documented
public @interface EnableConcreteAMQP {

    /**
     * 扫描concreteServices的包
     * <p>
     * 默认使用Configuration命名空间 concrete/amqp/当前appSet 下的 api.packages，
     * 如果为空，则使用Configuration命名空间 concrete/当前appSet 下的 api.packages
     *
     * @return 扫描concreteServices的包
     */
    String[] servicePackages() default {};

    /**
     * 额外需要注册的类
     * <p>
     * 默认使用Configuration命名空间 concrete/amqp/当前appSet 下的amqp.classes
     *
     * @return 额外需要注册的类
     */
    Class<?>[] classes() default {};

    String location() default "";

    String host() default "";

    int port() default -1;

    String virtualHost() default "";

    String username() default "";

    String password() default "";

    String exchangeName() default "";

    String queueName() default "";

    String executorName() default "";

    long ttl() default -1L;
}
