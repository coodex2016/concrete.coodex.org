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

import org.coodex.concrete.core.Level;
import org.coodex.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.*;
import java.io.IOException;
import java.io.OutputStream;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

@ConstrainedTo(RuntimeType.SERVER)
@PreMatching
public class ServerLogger extends AbstractLogger implements ContainerRequestFilter, ContainerResponseFilter {
    private final static Logger log = LoggerFactory.getLogger(ServerLogger.class);

    public ServerLogger() {
        super(log, Level.parse(Config.getValue("server","NONE", "jaxrs.logger.level",getAppSet())));
    }

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        if (!isEnabled()) {
            return;
        }
        final long id = _id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);

        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Server has received a request", id, context.getMethod(), context.getUriInfo().getRequestUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getHeaders());

        if (context.hasEntity() && printEntity(context.getMediaType())) {
            context.setEntityStream(
                    logInboundEntity(b, context.getEntityStream(), getCharset(context.getMediaType())));
        }

        log(b);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        if (!isEnabled()) {
            return;
        }
        final Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        final long id = requestId != null ? (Long) requestId : _id.incrementAndGet();

        final StringBuilder b = new StringBuilder();

        printResponseLine(b, "Server responded with a response", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getStringHeaders());

        if (responseContext.hasEntity() && printEntity(responseContext.getMediaType())) {
            final OutputStream stream = new LoggingStream(b, responseContext.getEntityStream());
            responseContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }
    }
}
