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

package org.coodex.concrete.client.spring;

import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.AbstractSyncInvoker;
import org.coodex.concrete.client.jaxrs.JaxRSClientCommon;
import org.coodex.concrete.client.jaxrs.JaxRSClientContext;
import org.coodex.concrete.client.jaxrs.JaxRSDestination;
import org.coodex.concrete.common.*;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.concrete.spring.components.SpringWebClientConfiguration;
import org.coodex.config.Config;
import org.coodex.mock.Mocker;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;
import static org.coodex.concrete.common.ConcreteHelper.isDevModel;
import static org.coodex.concrete.common.Token.CONCRETE_TOKEN_ID_KEY;
import static org.coodex.concrete.jaxrs.JaxRSHelper.getUnitFromContext;

public class SpringWebInvoker extends AbstractSyncInvoker {
    private static final Logger log = LoggerFactory.getLogger(SpringWebInvoker.class);


//    private static final Singleton<RestTemplate> REST_TEMPLATE = Singleton.with(
//            () -> {
//
//            }
//    );

    private static final SingletonMap<Boolean, RestTemplate> REST_TEMPLATES = SingletonMap
            .<Boolean, RestTemplate>builder()
            .function(key -> {
                if (key) {
                    try {
                        return BeanServiceLoaderProvider.getBeanProvider().getBean(RestTemplate.class);
                    } catch (Throwable th) {
                        log.warn("cannot get RestTemplate instance from BeanProvider.", th);
                        return SpringWebClientConfiguration.getDefaultRestTemplate();
                    }
                } else {
                    return SpringWebClientConfiguration.getDefaultRestTemplate();
                }
            })
            .build();


    private final boolean microService;

    SpringWebInvoker(Destination destination) {
        super(destination);
        if (destination instanceof SpringJaxRSDestination) {
            microService = ((SpringJaxRSDestination) destination).isMicroService();
        } else {
            microService = Config.getValue("microService", false, TAG_CLIENT, destination.getIdentify());
        }
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new JaxRSClientContext(getDestination(), context, "concrete-client-spring-web");
    }

    private HttpMethod getHttpMethod(String invokerType) {
        return HttpMethod.resolve(invokerType.toUpperCase());
    }

    @Override
    protected Object execute(Class<?> clz, Method method, Object[] args) throws Throwable {
        JaxrsUnit unit = getUnitFromContext(ConcreteHelper.getContext(method, clz));
        if (isDevModel("jaxrs.client")) {
            return Mocker.mockMethod(
                    unit.getMethod(),
                    unit.getDeclaringModule().getInterfaceClass());
        } else {
            JaxRSDestination destination = (JaxRSDestination) getDestination();
            RestTemplate restTemplate = REST_TEMPLATES.get(microService);
            String path = JaxRSClientCommon.getPath(unit, args, destination);
            Object body = JaxRSClientCommon.getSubmitObject(unit, args);
            HttpEntity<?> entity = body == null ? new HttpEntity<>(getHttpHeaders()) :
                    new HttpEntity<>(body, getHttpHeaders());
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                    path, getHttpMethod(unit.getInvokeType()), entity, byte[].class
            );
            JaxRSClientCommon.handleResponseHeaders(new MultivaluedHashMap<>(responseEntity.getHeaders()));
            return JaxRSClientCommon.processResult(
                    responseEntity.getStatusCodeValue(),
                    responseEntity.hasBody() ? bodyToString(responseEntity) : null,
                    unit,
                    responseEntity.getHeaders().containsKey(JaxRSHelper.HEADER_ERROR_OCCURRED),
                    path);
        }
    }

    private Charset getCharset(ResponseEntity<?> resp) {
        return Optional.ofNullable(resp.getHeaders().getContentType())
                .map(MediaType::getCharset)
                .orElse(StandardCharsets.UTF_8);
    }

    private String bodyToString(ResponseEntity<byte[]> resp) throws IOException {
        if (resp.getHeaders().entrySet().stream().anyMatch(e -> HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(e.getKey()) && !Common.isEmpty(e.getValue())
                && "gzip".equalsIgnoreCase(e.getValue().get(0)))) {
            try (GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(Objects.requireNonNull(resp.getBody())))) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    Common.copyStream(inputStream, outputStream);
                    return new String(outputStream.toByteArray(), getCharset(resp));
                }
            }
        } else {
            return new String(Objects.requireNonNull(resp.getBody()), getCharset(resp));
        }
//        if(resp.getHeaders().get(HttpHeaders.CONTENT_ENCODING))
    }


    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        JaxRSClientContext context = JaxRSClientCommon.getContext();
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, context.getLocale().toLanguageTag());
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        Subjoin subjoin = context.getSubjoin();
        String tokenId = ClientTokenManagement.getTokenId(getDestination(), context.getTokenId());
        if (subjoin != null || !Common.isBlank(tokenId)) {
            if (subjoin != null) {
                for (String key : subjoin.keySet()) {
                    headers.add(key, subjoin.get(key));
                }
            }
            if (!Common.isBlank(tokenId)) {
                headers.add(CONCRETE_TOKEN_ID_KEY, tokenId);
            }
        }
        return headers;
    }


}
