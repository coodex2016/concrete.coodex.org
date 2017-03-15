package org.coodex.concrete.core;

import org.coodex.concrete.common.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * 基于java.text.MessageFormat  的实现
 * Created by davidoff shen on 2016-09-04.
 */
public class JavaTextFormatMessageFormatter implements MessageFormatter {
    private final static Logger log = LoggerFactory.getLogger(JavaTextFormatMessageFormatter.class);

    @Override
    public String format(String pattern, Object... objects) {
        try {
            return MessageFormat.format(pattern, objects);
        } catch (IllegalArgumentException e) {
            log.warn("illegal argument :{}", pattern, e);
            return pattern;
        }
    }


}
