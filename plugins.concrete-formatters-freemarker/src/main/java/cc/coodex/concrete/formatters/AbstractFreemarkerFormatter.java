package cc.coodex.concrete.formatters;

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


    protected final String format(String template, Map<String, Object> values) throws IOException, TemplateException {
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
