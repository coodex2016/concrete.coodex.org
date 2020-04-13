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

package org.coodex.concrete.common;

import org.coodex.concrete.core.JavaTextFormatMessageFormatter;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;

//import org.coodex.concrete.core.ResourceBundlesMessagePatternLoader;

/**
 * Created by davidoff shen on 2017-05-08.
 */
public class AbstractMessageFacade {
    private final static MessageFormatter DEFAULT_MESSAGE_FORMATTER = new JavaTextFormatMessageFormatter();

//    @SuppressWarnings("deprecation")
//    private final static MessagePatternLoader DEFAULT_PATTERN_LOADER = new ResourceBundlesMessagePatternLoader();

    private final static LogFormatter DEFAULT_LOG_FORMATTER = (pattern, values) -> values == null ? null : values.toString();

    private final static ServiceLoader<MessageFormatter> MESSAGE_FORMATTER_SERVICE_LOADER = new ServiceLoaderImpl<MessageFormatter>(DEFAULT_MESSAGE_FORMATTER) {
    };

//    private final static ServiceLoader<MessagePatternLoader> MESSAGE_PATTERN_LOADER_SERVICE_LOADER = new ConcreteServiceLoader<MessagePatternLoader>() {
//        @Override
//        public MessagePatternLoader getConcreteDefaultProvider() {
//            return DEFAULT_PATTERN_LOADER;
//        }
//    };

    private final static ServiceLoader<LogFormatter> LOG_FORMATTER_SERVICE_LOADER = new ServiceLoaderImpl<LogFormatter>(DEFAULT_LOG_FORMATTER) {
    };

    public static MessageFormatter getFormatter(Class<? extends MessageFormatter> formatterClass) {

        MessageFormatter formatter = formatterClass == null || formatterClass == MessageFormatter.class ?
                MESSAGE_FORMATTER_SERVICE_LOADER.get() :
                MESSAGE_FORMATTER_SERVICE_LOADER.get(formatterClass);

        return formatter == null ? DEFAULT_MESSAGE_FORMATTER : formatter;
    }

//    public static MessagePatternLoader getPatternLoader(Class<? extends MessagePatternLoader> loaderClass) {
//        MessagePatternLoader patternLoader = loaderClass == null || loaderClass == MessagePatternLoader.class ?
//                MESSAGE_PATTERN_LOADER_SERVICE_LOADER.getInstance() :
//                MESSAGE_PATTERN_LOADER_SERVICE_LOADER.getInstance(loaderClass);
//
//        return patternLoader == null ? DEFAULT_PATTERN_LOADER : patternLoader;
//    }


    public static LogFormatter getLogFormatter(Class<? extends LogFormatter> formatterClass) {
        return formatterClass == null || formatterClass == LogFormatter.class ?
                LOG_FORMATTER_SERVICE_LOADER.get() :
                LOG_FORMATTER_SERVICE_LOADER.get(formatterClass);
    }
}
