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

package org.coodex.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum Level {


    NONE, TRACE, DEBUG, INFO, WARN, ERROR;

    private final static Logger logger = LoggerFactory.getLogger(Level.class);

    public static Level parse(String levelName) {
        try {
            return Level.valueOf(Level.class, levelName.toUpperCase());
        } catch (Throwable th) {
            if (!levelName.equalsIgnoreCase("NONE"))
                logger.warn(th.getLocalizedMessage());
            return NONE;
        }
    }

    public boolean isEnabled(Logger log) {
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

    public void log(Logger log, String str) {
        if (this == NONE) return;
        try {
            Method method = Logger.class.getMethod(this.name().toLowerCase(), String.class);
            method.setAccessible(true);
            method.invoke(log, str);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {
        }
    }
}
