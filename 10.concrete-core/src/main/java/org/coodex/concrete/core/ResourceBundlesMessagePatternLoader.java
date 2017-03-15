package org.coodex.concrete.core;

import org.coodex.closure.LocaleClosure;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.MessagePatternLoader;
import org.coodex.util.Common;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 基于 ResourceBundle 的实现
 * Created by davidoff shen on 2016-12-02.
 */
public class ResourceBundlesMessagePatternLoader implements MessagePatternLoader {
    public static final String MESSAGE_PATTERN = "messagePattern";


    private String getPatternFromBundle(String key) {
        for (String resource : ConcreteHelper.getProfile()
                .getStrList("messagePattern.resourceBundles", ",", new String[]{MESSAGE_PATTERN})) {

            Locale locale = LocaleClosure.get();
            if (Common.isBlank(resource) || Common.isBlank(resource.trim())) continue;
            try {
                String pattern = ResourceBundle.getBundle(resource, locale == null ? Locale.getDefault() : locale).getString(key);
                if (pattern != null) return pattern;
            } catch (Throwable t) {
            }
        }
        return null;
    }


    @Override
    public String getMessageTemplate(String key) {
        return getPatternFromBundle(key);
    }
}
