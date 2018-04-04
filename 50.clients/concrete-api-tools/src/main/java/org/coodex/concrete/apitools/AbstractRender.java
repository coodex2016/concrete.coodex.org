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

package org.coodex.concrete.apitools;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.coodex.util.Common;

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
    private String desc;

    protected String getRenderDesc() {
        return desc;
    }

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
        this.desc = desc;
        String renderName = getRenderName();
        renderName = renderName == null ? null : renderName.toLowerCase();
        return desc != null && desc.toLowerCase().startsWith(renderName);
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
        if (inputStream == null) {
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
