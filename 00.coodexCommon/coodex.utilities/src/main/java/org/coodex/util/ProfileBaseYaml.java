/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProfileBaseYaml extends Profile {

    private final static Logger log = LoggerFactory.getLogger(ProfileBaseYaml.class);
    private Map<String, Object> valuesMap = new HashMap<String, Object>();


    ProfileBaseYaml(String path) {
        try {
            init(path);
        } catch (IOException e) {
            log.warn("init [{}] yaml profile failed", path, e);
        }
    }

    private void init(String path) throws IOException {
        Yaml yaml = new Yaml();
        URL url = Common.getResource(path);
        if (url != null) {
            InputStream is = url.openStream();
            try {
                Map<String, Object> map = yaml.load(is);
                map(null, map);
            } finally {
                is.close();
            }
        } else {
            log.info("{} not found.", path);
        }
    }

    private void map(String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix == null ? entry.getKey() : (prefix + "." + entry.getKey());
            Object value = entry.getValue();
            if (value == null) {
                valuesMap.put(key, null);
            }
            if (value instanceof Map) {
                //noinspection unchecked
                map(key, (Map<String, Object>) value);
            } else {
                valuesMap.put(key, value);
            }
        }
    }

    private String toString(Object o) {
        if (o == null) return null;
        Class type = o.getClass();
        if (type.isArray()) {
            Object[] array = (Object[]) o;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(toString(array[i]));
            }
            return builder.toString();
        } else if (Collection.class.isAssignableFrom(type)) {
            boolean appendSP = false;
            for (Object el : (Collection) o) {
                StringBuilder builder = new StringBuilder();
                if (appendSP) {
                    builder.append(", ");
                } else {
                    appendSP = true;
                }
                builder.append(toString(el));
                return builder.toString();
            }
        }
        return o.toString();
    }

    @Override
    public String getStringImpl(String key) {
        Object o = valuesMap.get(key);
        if (o == null) return null;
        return toString(o);
    }

    @Override
    public boolean getBool(String key, boolean v) {
        Object o = valuesMap.get(key);
        if (o == null) return v;
        if (o.getClass().equals(Boolean.class) || o.getClass().equals(boolean.class)) {
            return (Boolean) o;
        } else
            return super.getBool(key, v);
    }

    @Override
    public int getInt(String key, int v) {
        // todo 优化
        return super.getInt(key, v);
    }

    @Override
    public long getLong(String key, long v) {
        // todo 优化
        return super.getLong(key, v);
    }

    @Override
    public String[] getStrList(String key, String delim, String[] v) {
        // todo 优化
        return super.getStrList(key, delim, v);
    }
}
