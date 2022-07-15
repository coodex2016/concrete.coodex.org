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
import org.coodex.util.LazyServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

@SuppressWarnings({"SpringComponentScan"})
@Configuration
@ComponentScan({
        "org.coodex.spring",
        "org.coodex.concrete.spring.components",
        "org.coodex.concrete.**.injectable"})
public class ConcreteSpringConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ConcreteSpringConfiguration.class);


    private static final LazyServiceLoader<InterceptorLoader> INTERCEPTOR_LOADER = new LazyServiceLoader<InterceptorLoader>(ConcreteSpringConfiguration::getInterceptorSupportedMap) {
    };

    private static Map<String, Class<? extends ConcreteInterceptor>> getInterceptorSupportedMap() {
        Map<String, Class<? extends ConcreteInterceptor>> map = new HashMap<>();
        map.put("rbac", RBACInterceptor.class);
        map.put("limiting", LimitingInterceptor.class);
        map.put("signature", SignatureInterceptor.class);
        map.put("timing", ServiceTimingInterceptor.class);
        map.put("beanValidation", BeanValidationInterceptor.class);
        return map;
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
        for (Map.Entry<String, Class<? extends ConcreteInterceptor>> entry : INTERCEPTOR_LOADER.get().getInterceptorSupportedMap().entrySet()) {

            if (Config.getValue("interceptors." + entry.getKey(), false, getAppSet())) {
                try {
                    list.add(entry.getValue().getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                         InvocationTargetException e) {
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
