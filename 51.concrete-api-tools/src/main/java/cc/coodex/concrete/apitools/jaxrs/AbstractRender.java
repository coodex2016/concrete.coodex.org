package cc.coodex.concrete.apitools.jaxrs;

import cc.coodex.concrete.apitools.ConcreteAPIRender;
import cc.coodex.util.Common;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public abstract class AbstractRender implements ConcreteAPIRender {

    protected static final String FS = Common.FILE_SEPARATOR;
    private Configuration configuration;
    private String rootToWrite;

    @Override
    public void setRoot(String rootPath) {
        this.rootToWrite = rootPath;
    }

    private synchronized Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration(Configuration.getVersion());
            configuration.setClassLoaderForTemplateLoading(this.getClass().getClassLoader(), getTemplatePath());
        }
        return configuration;
    }

    protected abstract String getTemplatePath();

    protected abstract String getRenderName();

    @Override
    public boolean isAccept(String desc) {
        return desc != null && desc.equalsIgnoreCase(getRenderName());
    }

    private Template getTemplate(String templateName) throws IOException {
        return getConfiguration().getTemplate(templateName);
    }

    public void writeTo(String filePath, String templateName, String pojoKey, Object value) throws IOException {
        writeTo(filePath, templateName, pojoKey, value, null);
    }

    public void writeTo(String filePath, String templateName, String pojoKey, Object value, Object toolKit) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(pojoKey, value);
        if (toolKit != null) {
            map.put("tool", toolKit);
        }
        writeTo(filePath, templateName, map);
    }

    public void writeTo(String filePath, String templateName, Map<String, Object> map) throws IOException {
        Template template = getTemplate(templateName);
        File target = Common.getNewFile(rootToWrite + FS + filePath);
        OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(target), Charset.forName("UTF-8"));
        try {
            template.process(map, outputStream);
        } catch (TemplateException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        } finally {
            outputStream.close();
        }
    }

    protected boolean exists(String file) {
        return new File(rootToWrite + FS + file).exists();
    }

    protected void copyTo(String resourceName, String path) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(getTemplatePath() + resourceName);
        if(inputStream == null){
            throw new IOException("not found: " + getTemplatePath() + resourceName);
        }
        try {
            File target = Common.getNewFile(rootToWrite + FS + path);
            OutputStream targetStream = new FileOutputStream(target);
            try {
                Common.copyStream(inputStream, targetStream);
            } finally {
                targetStream.close();
            }
        } finally {
            inputStream.close();
        }
    }
}
