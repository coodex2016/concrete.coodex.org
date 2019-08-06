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

package org.coodex.concrete.jaxrs.logging;

import org.coodex.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;
import java.io.OutputStream;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

@ConstrainedTo(RuntimeType.CLIENT)
@PreMatching
public class ClientLogger extends AbstractLogger implements ClientRequestFilter, ClientResponseFilter {

    private final static Logger log = LoggerFactory.getLogger(ClientLogger.class);

    public ClientLogger() {
        this(log);
    }

    public ClientLogger(Logger log) {
        super(log, Level.parse(Config.getValue("client", "DEBUG", "jaxrs.logger.level", getAppSet())));
    }

    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        if (!isEnabled()) {
            return;
        }
        final long id = _id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);

        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Sending client request", id, context.getMethod(), context.getUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getStringHeaders());

        if (context.hasEntity() && printEntity(context.getMediaType())) {
            final OutputStream stream = new LoggingStream(b, context.getEntityStream());
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }
    }

    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext)
            throws IOException {
        if (!isEnabled()) {
            return;
        }
        final Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        final long id = requestId != null ? (Long) requestId : _id.incrementAndGet();

        final StringBuilder b = new StringBuilder();

        printResponseLine(b, "Client response received", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getHeaders());

        if (responseContext.hasEntity() && printEntity(responseContext.getMediaType())) {
            responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream(),
                    getCharset(responseContext.getMediaType())));
        }

        log(b);
    }
}
