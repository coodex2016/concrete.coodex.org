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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public abstract class AbstractRenderer implements ConcreteAPIRenderer {

    protected static final String FS = Common.FILE_SEPARATOR;
    private Configuration configuration;
    private String rootToWrite;
    private String desc;

    private Map<String, Object> ext = null;

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
        if (renderName == null) return false;
        return desc != null && desc.toLowerCase().startsWith(renderName.toLowerCase());
    }

    private Template getTemplate(String templateName) throws IOException {
        return getConfiguration().getTemplate(templateName);
    }

    public void writeTo(String filePath, String templateName, String pojoKey, Object value) throws IOException {
        writeTo(filePath, templateName, pojoKey, value, null);
    }

    public void writeTo(String filePath, String templateName, String pojoKey, Object value, Object toolKit) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(pojoKey, value);
        if (toolKit != null) {
            map.put("tool", toolKit);
        }
        writeTo(filePath, templateName, map);
    }

    protected Map<String, Object> merge(Map<String, Object> map) {
        if (ext == null || ext.size() == 0) {
            return map;
        } else {
            Map<String, Object> result = new HashMap<>();
            if (map != null) {
                result.putAll(map);
            }
            result.putAll(ext);
            return result;
        }
    }

    public void writeTo(String filePath, String templateName, Map<String, Object> map) throws IOException {
        Template template = getTemplate(templateName);
        File target = Common.newFile(rootToWrite + FS + filePath);
        try (OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            template.process(merge(map), outputStream);
        } catch (TemplateException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
            File target = Common.newFile(rootToWrite + FS + path);
            try (OutputStream targetStream = new FileOutputStream(target)) {
                Common.copyStream(inputStream, targetStream);
            }
        } finally {
            inputStream.close();
        }
    }

    @Override
    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }
}
