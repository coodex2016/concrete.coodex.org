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

package org.coodex.concrete.spring;

import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.config.Config;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

@Configuration
@ComponentScan({
        "org.coodex.concrete.spring.components",
        "org.coodex.concrete.**.injectable"
})
public class ConcreteSpringConfiguration {

    private final static Logger log = LoggerFactory.getLogger(ConcreteSpringConfiguration.class);

    private static Singleton<ServiceLoader<InterceptorLoader>> INTERCEPTOR_LOADER =
            new Singleton<>(() -> new ServiceLoaderImpl<InterceptorLoader>(ConcreteSpringConfiguration::getInterceptorSupportedMap) {
            });

    @Bean
    public BeanProvider springBeanProvider() {
        return new SpringBeanProvider();
    }

    @Bean
    public Token tokenWrapper() {
        return TokenWrapper.getInstance();
    }

    @Bean
    public Caller callerWrapper() {
        return CallerWrapper.getInstance();
    }

    @Bean
    public Subjoin subjoinWrapper() {
        return SubjoinWrapper.getInstance();
    }

    private static Map<String, Class<? extends ConcreteInterceptor>> getInterceptorSupportedMap() {
        return new HashMap<String, Class<? extends ConcreteInterceptor>>() {{
            put("rbac", RBACInterceptor.class);
            put("limiting", LimitingInterceptor.class);
            put("signature", SignatureInterceptor.class);
            put("log", OperationLogInterceptor.class);
            put("timing", ServiceTimingInterceptor.class);
            put("beanValidation", BeanValidationInterceptor.class);
        }};
    }

    @Bean
    public Set<ConcreteInterceptor> interceptors() {
        Set<ConcreteInterceptor> set = new HashSet<>();
        for (Map.Entry<String, Class<? extends ConcreteInterceptor>> entry :
                INTERCEPTOR_LOADER.get().get().getInterceptorSupportedMap().entrySet()) {

            if (Config.getValue("interceptors." + entry.getKey(), false, "concrete", getAppSet())) {
                try {
                    set.add(entry.getValue().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    log.warn("load interceptor {}[{}] failed.", entry.getKey(), entry.getValue().getName());
                }
            }
        }
        return set;
    }
}
