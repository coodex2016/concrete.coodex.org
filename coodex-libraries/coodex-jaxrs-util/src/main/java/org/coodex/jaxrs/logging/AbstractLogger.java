/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.jaxrs.logging;

import org.coodex.logging.Level;
import org.slf4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 参考jersey实现编写
 */
public class AbstractLogger implements WriterInterceptor {

    /**
     * Prefix will be printed before requests
     */
    protected static final String REQUEST_PREFIX = "> ";
    /**
     * Prefix will be printed before response
     */
    protected static final String RESPONSE_PREFIX = "< ";
    /**
     * The entity stream property
     */
    protected static final String ENTITY_LOGGER_PROPERTY = AbstractLogger.class.getName() + ".entityLogger";
    /**
     * Logging record id property
     */
    protected static final String LOGGING_ID_PROPERTY = AbstractLogger.class.getName() + ".id";
    protected static final int MAX_ENTITY_SIZE = 8192;
    private static final String NOTIFICATION_PREFIX = "* ";
    private static final MediaType TEXT_MEDIA_TYPE = new MediaType("text", "*");
    private static final Set<MediaType> READABLE_APP_MEDIA_TYPES = new HashSet<>();

    static {
        {
            READABLE_APP_MEDIA_TYPES.add(TEXT_MEDIA_TYPE);
            READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_ATOM_XML_TYPE);
            READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
            READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_JSON_TYPE);
            READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_SVG_XML_TYPE);
            READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_XHTML_XML_TYPE);
            READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_XML_TYPE);
        }

    }

    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
            (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());
    protected final AtomicLong _id = new AtomicLong(0);
    private final Logger log;
    private final Level level;

    public AbstractLogger(Logger log, Level level) {
        this.log = log;
        this.level = level;
    }

    /**
     * Returns {@code true} if specified {@link MediaType} is considered textual.
     * <p>
     * See {@link #READABLE_APP_MEDIA_TYPES}.
     *
     * @param mediaType the media type of the entity
     * @return {@code true} if specified {@link MediaType} is considered textual.
     */
    protected static boolean isReadable(MediaType mediaType) {
        if (mediaType != null) {
            for (MediaType readableMediaType : READABLE_APP_MEDIA_TYPES) {
                if (readableMediaType.isCompatible(mediaType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if entity has to be printed.
     *
     * @param mediaType the media type of the payload.
     * @return {@code true} if entity has to be printed.
     */
    protected static boolean printEntity(MediaType mediaType) {
        return isReadable(mediaType);
    }

    protected static Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? StandardCharsets.UTF_8 : Charset.forName(name);
    }

    protected boolean isEnabled() {
        return level.isEnabled(log);
    }

    /**
     * Logs a {@link StringBuilder} parameter at required level.
     *
     * @param b message to log
     */
    protected void log(final StringBuilder b) {
        if (level.isEnabled(log)) {
            level.log(log, b.toString());
        }
    }

    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append(id).append(" ");
        return b;
    }

    protected void printRequestLine(final StringBuilder b, final String note, final long id, final String method, final URI uri) {
        prefixId(b, id).append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName())
                .append("\n");
        prefixId(b, id).append(REQUEST_PREFIX).append(method).append(" ")
                .append(uri.toASCIIString()).append("\n");
    }

    protected void printResponseLine(final StringBuilder b, final String note, final long id, final int status) {
        prefixId(b, id).append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName()).append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX)
                .append(status)
                .append("\n");
    }

    protected void printPrefixedHeaders(final StringBuilder b,
                                        final long id,
                                        final String prefix,
                                        final MultivaluedMap<String, String> headers) {
        for (final Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
            final List<String> val = headerEntry.getValue();
            final String header = headerEntry.getKey();
            // TODO decode
            if (val.size() == 1) {
                String value = val.get(0);
                try {
                    String decoded = URLDecoder.decode(value, "UTF-8");
                    if (!value.equals(decoded)) {
                        value += "[decoded value:" + decoded + "]";
                    }
                } catch (UnsupportedEncodingException ignore) {// NOSONAR
                }
                prefixId(b, id).append(prefix).append(header).append(": ").append(value).append("\n");
            } else {
                final StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (final String s : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    String value = s;
                    try {
                        String decoded = URLDecoder.decode(value, "UTF-8");
                        if (!value.equals(decoded)) {
                            value += "[decoded value:" + decoded + "]";
                        }
                    } catch (UnsupportedEncodingException ignore) {// NOSONAR
                    }
                    sb.append(value);
                }
                prefixId(b, id).append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }

    Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {
        final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<>(COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    protected InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(MAX_ENTITY_SIZE + 1);
        final byte[] entity = new byte[MAX_ENTITY_SIZE + 1];
        final int entitySize = stream.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, MAX_ENTITY_SIZE), charset));
        if (entitySize > MAX_ENTITY_SIZE) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (printEntity(writerInterceptorContext.getMediaType()) && stream != null) {
            log(stream.getStringBuilder(getCharset(writerInterceptorContext.getMediaType())));
        }
    }


    /**
     * Helper class used to log an entity to the output stream up to the specified maximum number of bytes.
     */
    protected static class LoggingStream extends FilterOutputStream {

        private final StringBuilder b;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /**
         * Creates {@code LoggingStream} with the entity and the underlying output stream as parameters.
         *
         * @param b     contains the entity to log.
         * @param inner the underlying output stream.
         */
        public LoggingStream(final StringBuilder b, final OutputStream inner) {
            super(inner);

            this.b = b;
        }

        public StringBuilder getStringBuilder(final Charset charset) {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, MAX_ENTITY_SIZE), charset));
            if (entity.length > MAX_ENTITY_SIZE) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= MAX_ENTITY_SIZE) {
                baos.write(i);
            }
            out.write(i);
        }
    }

}
