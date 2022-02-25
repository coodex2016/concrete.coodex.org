/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring.components;

import org.coodex.concrete.client.spring.LoggingInterceptor;
import org.coodex.util.Singleton;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SuppressWarnings("SpringFacetCodeInspection")
@Configuration
public class SpringWebClientConfiguration {

    public static final Singleton<LoggingInterceptor> LOGGING_INTERCEPTOR_SINGLETON = Singleton.with(
            LoggingInterceptor::new
    );

    public static RestTemplate getDefaultRestTemplate() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
//        simpleClientHttpRequestFactory.setConnectTimeout(Config.getValue("client.defaultConnectTimeout", 500));
//        simpleClientHttpRequestFactory.setReadTimeout(Config.getValue("client.defaultReadTimeout", 300));
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory));
        restTemplate.setInterceptors(Collections.singletonList(LOGGING_INTERCEPTOR_SINGLETON.get()));
        return restTemplate;
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return getDefaultRestTemplate();
    }
}
