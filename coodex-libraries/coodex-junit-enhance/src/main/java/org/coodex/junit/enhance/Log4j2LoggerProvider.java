/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.junit.enhance;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.coodex.config.Config;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class Log4j2LoggerProvider extends AbstractLoggerProvider {
    private final static Singleton<String> CONSOLE_APPENDER_KEY
            = Singleton.with(() -> Config.getValue("logger.appender.console", "Console"));

    protected String getFileAppenderDefaultPattern() {
        return "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n";
    }

    protected String getDefaultPath() {
        return "logs/";
    }

    private Logger newLogger(String loggerName) {
        LoggerContext ctx = LoggerContext.getContext(false);
        Configuration configuration = ctx.getConfiguration();
        // new Appender
        newAppender(loggerName, configuration);
        // new LoggerConfig
        newLoggerConfig(loggerName, configuration);

        ctx.updateLoggers();
        return LoggerFactory.getLogger(loggerName);
    }

    private void buildConsoleAppender(Configuration configuration) {
        Appender appender = ConsoleAppender.newBuilder()
                .setName(CONSOLE_APPENDER_KEY.get())
//                .withName(CONSOLE_APPENDER_KEY.get())
                .setLayout(
//                .withLayout(
                        PatternLayout.newBuilder()
                                .withCharset(StandardCharsets.UTF_8)
                                .withPattern(Config.getValue("logger.console.pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"))
                                .withConfiguration(configuration)
                                .build()
                )
                .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                .withImmediateFlush(true)
                .setConfiguration(configuration)
                .build();
        appender.start();
        configuration.addAppender(appender);
    }

    private void newLoggerConfig(String loggerName, Configuration configuration) {

        Level level = Level.toLevel(
                Config.getValue("logger." + loggerName + ".level", Config.get("logger.level")),
                Level.INFO);
        boolean console = Config.getValue("logger.console", true);
        if (console && configuration.getAppender(CONSOLE_APPENDER_KEY.get()) == null) {
            buildConsoleAppender(configuration);
        }
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, level,
                loggerName, "true", console ? new AppenderRef[]{
                        AppenderRef.createAppenderRef(loggerName, level, null),
                        AppenderRef.createAppenderRef(CONSOLE_APPENDER_KEY.get(), level, null),
                } : new AppenderRef[]{
                        AppenderRef.createAppenderRef(loggerName, level, null),
                },
                null, configuration, null);

        loggerConfig.addAppender(configuration.getAppender(loggerName), level, null);
        if (console) {
            loggerConfig.addAppender(configuration.getAppender(CONSOLE_APPENDER_KEY.get()), level, null);
        }

        configuration.addLogger(loggerName, loggerConfig);
    }

    private void newAppender(String loggerName, Configuration configuration) {
        Appender appender = FileAppender.newBuilder()
                .setConfiguration(configuration)
                .setName(loggerName)
//                .withName(loggerName)
                .setLayout(
//                .withLayout(
                        PatternLayout.newBuilder()
                                .withCharset(StandardCharsets.UTF_8)
                                .withPattern(Config.getValue("logger." + loggerName + ".pattern",
                                        Config.getValue("logger.pattern",
                                                getFileAppenderDefaultPattern())))
                                .build()
                )
                .withAppend(false)
                .withFileName(Config.getValue("logger.path", getDefaultPath()) + loggerName + ".log")
                .build();
        appender.start();
        configuration.addAppender(appender);
    }


    @Override
    protected Logger build(String name) {
        return newLogger(name);
    }
}
