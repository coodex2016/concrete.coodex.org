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

import org.coodex.concrete.client.jaxrs.JaxRSClientContext;
import org.coodex.concrete.client.jaxrs.JaxRSDestination;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.config.Config;
import org.coodex.logging.Level;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static final AtomicLong REQUEST_ID = new AtomicLong(0);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        boolean trace = false;
        ServiceContext context = ConcreteContext.getServiceContext();
        Level level = null;
        Charset charset = StandardCharsets.UTF_8;
        if (context instanceof JaxRSClientContext) {
            JaxRSClientContext jaxRSClientContext = (JaxRSClientContext) context;
            JaxRSDestination jaxRSDestination = (JaxRSDestination) jaxRSClientContext.getDestination();
            String strLevel = jaxRSDestination.getLogLevel();
            if (strLevel == null) {
                strLevel = Config.getValue("client", "DEBUG", "jaxrs.logger.level", getAppSet());
            }
            level = Level.parse(strLevel);
            trace = level.isEnabled(log);
            if (!Common.isBlank(jaxRSDestination.getCharset())) {
                charset = Charset.forName(jaxRSDestination.getCharset());
            }
        }
        long id = REQUEST_ID.incrementAndGet();
        if (trace)
            traceRequest(request, body, id, level, charset);
        ClientHttpResponse response = execution.execute(request, body);
        if (trace)
            traceResponse(response, id, level);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body, long id, Level level, Charset charset) throws IOException {
        if (level == null) return;
        StringBuilder builder = new StringBuilder("request: \n");
        builder.append(id).append(" > ").append(request.getMethod()).append(" ")
                .append(request.getURI()).append('\n');
        request.getHeaders().forEach((key, list) -> {
            StringJoiner joiner = new StringJoiner("; ");
            list.forEach(joiner::add);
            builder.append(id).append(" > ").append(key).append(": ")
                    .append(joiner).append('\n');
        });

        builder.append(new String(body, charset)).append('\n');
        level.log(log, builder.toString());
    }

    private Charset getCharset(HttpHeaders headers) {
        return Optional.ofNullable(headers.getContentType()).map(MediaType::getCharset).orElse(StandardCharsets.UTF_8);
    }

    private InputStream getBodyInputStream(ClientHttpResponse resp) throws IOException {
        return resp.getHeaders().entrySet().stream().anyMatch(e -> HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(e.getKey()) && !Common.isEmpty(e.getValue())
                && "gzip".equalsIgnoreCase(e.getValue().get(0))) ?
                new GZIPInputStream(resp.getBody()) :
                resp.getBody();
    }

    private void traceResponse(ClientHttpResponse response, long id, Level level) throws IOException {
        if (level == null) return;
        StringBuilder builder = new StringBuilder("response: \n");
        builder.append(id).append(" < ").append(response.getRawStatusCode()).append(" ")
                .append(response.getStatusText()).append('\n');

        response.getHeaders().forEach((key, list) -> {
            StringJoiner joiner = new StringJoiner("; ");
            list.forEach(joiner::add);
            builder.append(id).append(" < ").append(key).append(": ")
                    .append(joiner).append('\n');
        });

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        getBodyInputStream(response),
                        getCharset(response.getHeaders())
                )
        );
        String line = bufferedReader.readLine();
        while (line != null) {
            builder.append(line);
            builder.append('\n');
            line = bufferedReader.readLine();
        }
        level.log(log, builder.toString());
    }
}
