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

package org.coodex.concrete.formatters;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-02.
 */
public class AbstractFreemarkerFormatter {
    private static final Configuration FREEMARKER = new Configuration(Configuration.getVersion());

    private static final StringTemplateLoader TEMPLATE_LOADER = new StringTemplateLoader();

    static {
        FREEMARKER.setTemplateLoader(TEMPLATE_LOADER);
    }

    private Template getTemplate(String template) throws IOException {
        synchronized (TEMPLATE_LOADER) {
            TEMPLATE_LOADER.putTemplate(template, template);
        }
        return FREEMARKER.getTemplate(template);
    }


    protected final String formatMsg(String template, Map<String, Object> values) throws IOException, TemplateException {
        if(template == null) return null;
        Template t = getTemplate(template);
        Writer writer = new StringWriter();
        try {
            t.process(values, writer);
            return writer.toString();
        } finally {
            writer.close();
        }
    }
}
