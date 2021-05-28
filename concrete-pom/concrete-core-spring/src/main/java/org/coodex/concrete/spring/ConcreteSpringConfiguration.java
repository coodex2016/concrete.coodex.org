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

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.config.Config;
import org.coodex.spring.SpringServiceLoaderProvider;
import org.coodex.util.ActiveProfilesProvider;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

@SuppressWarnings({"SpringComponentScan"})
@Configuration
@ComponentScan({
        "org.coodex.concrete.spring.components",
        "org.coodex.concrete.**.injectable"
})
public class ConcreteSpringConfiguration {

    private final static Logger log = LoggerFactory.getLogger(ConcreteSpringConfiguration.class);

//    private static Singleton<ServiceLoader<InterceptorLoader>> INTERCEPTOR_LOADER =
//            new Singleton<>(() -> new ServiceLoaderImpl<InterceptorLoader>(ConcreteSpringConfiguration::getInterceptorSupportedMap) {
//            });

    private static final LazyServiceLoader<InterceptorLoader> INTERCEPTOR_LOADER =
            new LazyServiceLoader<InterceptorLoader>(ConcreteSpringConfiguration::getInterceptorSupportedMap) {
            };
//            new Singleton<>(() -> new ServiceLoaderImpl<InterceptorLoader>(ConcreteSpringConfiguration::getInterceptorSupportedMap) {
//            });

    private static Map<String, Class<? extends ConcreteInterceptor>> getInterceptorSupportedMap() {
        return new HashMap<String, Class<? extends ConcreteInterceptor>>() {{
            put("rbac", RBACInterceptor.class);
            put("limiting", LimitingInterceptor.class);
            put("signature", SignatureInterceptor.class);
//            put("log", OperationLogInterceptor.class);
            put("timing", ServiceTimingInterceptor.class);
            put("beanValidation", BeanValidationInterceptor.class);
        }};
    }

    @Bean
    public BeanProvider springBeanProvider() {
        return new SpringBeanProvider();
    }

    @Bean
    public ActiveProfilesProvider springActiveProfilesProvider() {
        return new org.coodex.spring.SpringActiveProfileProvider();
    }

    @Bean
    public ServiceLoaderProvider springServiceLoaderProvider() {
        return new SpringServiceLoaderProvider();
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

    @Bean
    public ConcreteInterceptor interceptors() {
        List<ConcreteInterceptor> list = new ArrayList<>();
        for (Map.Entry<String, Class<? extends ConcreteInterceptor>> entry :
                INTERCEPTOR_LOADER.get().getInterceptorSupportedMap().entrySet()) {

            if (Config.getValue("interceptors." + entry.getKey(), false, getAppSet())) {
                try {
                    list.add(entry.getValue().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    log.warn("load interceptor {}[{}] failed.", entry.getKey(), entry.getValue().getName());
                }
            }
        }
        switch (list.size()) {
            case 0:
                return new ConcreteInterceptor() {
                    @Override
                    public boolean accept(DefinitionContext context) {
                        return false;
                    }

                    @Override
                    public void before(DefinitionContext context, MethodInvocation joinPoint) {

                    }

                    @Override
                    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
                        return result;
                    }

                    @Override
                    public Throwable onError(DefinitionContext context, MethodInvocation joinPoint, Throwable th) {
                        return th;
                    }

                    @Override
                    public int getOrder() {
                        return 0;
                    }
                };
            case 1:
                return list.get(0);
            default:
                return new AsyncInterceptorChain(list);
        }
    }
}
