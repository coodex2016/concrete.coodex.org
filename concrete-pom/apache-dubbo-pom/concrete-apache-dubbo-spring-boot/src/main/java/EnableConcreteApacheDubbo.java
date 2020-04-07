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

import org.coodex.concrete.spring.ConcreteSpringConfigurationBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

import static org.coodex.concrete.dubbo.DubboConfigCaching.DEFAULT_APPLICATION_NAME;
import static org.coodex.concrete.dubbo.DubboConfigCaching.DEFAULT_VERSION;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        ConcreteSpringConfigurationBeanDefinitionRegistrar.class,
        ConcreteDubboBeanDefinitionRegistrar.class
})
@Documented
public @interface EnableConcreteApacheDubbo {

    /**
     * 扫描concreteServices的包
     * <p>
     * 默认使用Configuration命名空间 concrete/dubbo/当前appSet 下的 api.packages，
     * 如果为空，则使用Configuration命名空间 concrete/当前appSet 下的 api.packages
     *
     * @return 扫描concreteServices的包
     */
    String[] servicePackages() default {};

    /**
     * 额外需要注册的类
     * <p>
     * 默认使用Configuration命名空间 concrete/dubbo/当前appSet 下的dubbo.classes
     *
     * @return 额外需要注册的类
     */
    Class<?>[] classes() default {};

    /**
     * @return dubbo服务协议，参见 https://dubbo.apache.org/en-us/docs/user/references/protocol/introduction.html
     */
    String[] protocols() default {"dubbo"};

    /**
     * @return dubbo服务注册中心，参见 https://dubbo.apache.org/en-us/docs/user/references/registry/introduction.html
     */
    String[] registries() default {};

    /**
     * @return dubbo服务的应用名称，默认{@link org.coodex.concrete.dubbo.DubboConfigCaching#DEFAULT_APPLICATION_NAME}
     */
    String applicationName() default DEFAULT_APPLICATION_NAME;

    /**
     * @return dubbo服务版本，默认{@link org.coodex.concrete.dubbo.DubboConfigCaching#DEFAULT_VERSION}
     */
    String version() default DEFAULT_VERSION;
}
