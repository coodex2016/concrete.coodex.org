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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    static final String REQUEST_PREFIX = "> ";
    /**
     * Prefix will be printed before response
     */
    static final String RESPONSE_PREFIX = "< ";
    /**
     * The entity stream property
     */
    static final String ENTITY_LOGGER_PROPERTY = AbstractLogger.class.getName() + ".entityLogger";
    /**
     * Logging record id property
     */
    static final String LOGGING_ID_PROPERTY = AbstractLogger.class.getName() + ".id";
    final static int MAX_ENTITY_SIZE = 8192;
    private final static Logger logger = LoggerFactory.getLogger(AbstractLogger.class);
    private static final String NOTIFICATION_PREFIX = "* ";
    private static final MediaType TEXT_MEDIA_TYPE = new MediaType("text", "*");
    private static final Set<MediaType> READABLE_APP_MEDIA_TYPES = new HashSet<MediaType>() {{
        add(TEXT_MEDIA_TYPE);
        add(MediaType.APPLICATION_ATOM_XML_TYPE);
        add(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        add(MediaType.APPLICATION_JSON_TYPE);
        add(MediaType.APPLICATION_SVG_XML_TYPE);
        add(MediaType.APPLICATION_XHTML_XML_TYPE);
        add(MediaType.APPLICATION_XML_TYPE);
    }};
    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
            (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());
    final AtomicLong _id = new AtomicLong(0);
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
    static boolean isReadable(MediaType mediaType) {
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
    static boolean printEntity(MediaType mediaType) {
        return isReadable(mediaType);
    }

    static Charset getCharset(MediaType m) {
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
    void log(final StringBuilder b) {
        if (level.isEnabled(log)) {
            level.log(log, b.toString());
        }
    }

    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append(id).append(" ");
        return b;
    }

    void printRequestLine(final StringBuilder b, final String note, final long id, final String method, final URI uri) {
        prefixId(b, id).append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName())
                .append("\n");
        prefixId(b, id).append(REQUEST_PREFIX).append(method).append(" ")
                .append(uri.toASCIIString()).append("\n");
    }

    void printResponseLine(final StringBuilder b, final String note, final long id, final int status) {
        prefixId(b, id).append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName()).append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX)
                .append(status)
                .append("\n");
    }

    void printPrefixedHeaders(final StringBuilder b,
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
                } catch (UnsupportedEncodingException ignore) {
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
                    } catch (UnsupportedEncodingException ignore) {
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

    InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
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
        if (printEntity(writerInterceptorContext.getMediaType())) {
            if (stream != null) {
                log(stream.getStringBuilder(getCharset(writerInterceptorContext.getMediaType())));
            }
        }
    }

    enum Level {
        NONE, TRACE, DEBUG, INFO, WARN, ERROR;

        static Level parse(String levelName) {
            try {
                return Level.valueOf(Level.class, levelName.toUpperCase());
            } catch (Throwable th) {
                if (!levelName.equalsIgnoreCase("NONE"))
                    logger.warn(th.getLocalizedMessage());
                return NONE;
            }
        }

        boolean isEnabled(Logger log) {
            switch (this.name().toLowerCase()) {
                case "none":
                    return false;
                case "trace":
                    return log.isTraceEnabled();
                case "debug":
                    return log.isDebugEnabled();
                case "info":
                    return log.isInfoEnabled();
                case "warn":
                    return log.isWarnEnabled();
                case "error":
                    return log.isErrorEnabled();
            }
            return false;
        }

        void log(Logger log, String str) {
            if (this == NONE) return;
            try {
                Method method = Logger.class.getMethod(this.name().toLowerCase(), String.class);
                method.setAccessible(true);
                method.invoke(log, str);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {
            }
        }
    }

    /**
     * Helper class used to log an entity to the output stream up to the specified maximum number of bytes.
     */
    static class LoggingStream extends FilterOutputStream {

        private final StringBuilder b;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /**
         * Creates {@code LoggingStream} with the entity and the underlying output stream as parameters.
         *
         * @param b     contains the entity to log.
         * @param inner the underlying output stream.
         */
        LoggingStream(final StringBuilder b, final OutputStream inner) {
            super(inner);

            this.b = b;
        }

        StringBuilder getStringBuilder(final Charset charset) {
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
