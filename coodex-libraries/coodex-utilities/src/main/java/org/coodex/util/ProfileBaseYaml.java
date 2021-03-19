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
    private final Map<String, Object> valuesMap = new HashMap<>();

//    @Deprecated
//    ProfileBaseYaml(String path) {
//        try {
//            init(path);
//        } catch (IOException e) {
//            log.warn("init [{}] yaml profile failed", path, e);
//        }
//    }

    ProfileBaseYaml(URL url) {
        try {
            init(url);
        } catch (IOException e) {
            log.warn("init [{}] yaml profile failed", url, e);
        }
    }

    private void init(URL url) throws IOException {
        Yaml yaml = new Yaml();
        if (url != null) {
            try (InputStream is = url.openStream()) {
                Map<Object, Object> map = yaml.load(is);
                map(null, map);
            }
        }
    }

//    private void init(String path) throws IOException {
//        init(Common.getResource(path), path);
//    }

    private void map(String prefix, Map<Object, Object> map) {
        if (map == null) return;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = prefix == null ? entry.getKey().toString() : (prefix + "." + entry.getKey().toString());
            Object value = entry.getValue();
            if (value == null) {
                valuesMap.put(key, null);
            }
            if (value instanceof Map) {
                map(key, Common.cast(value));
            } else {
                valuesMap.put(key, value);
            }
        }
    }

    private String toString(Object o) {
        if (o == null) return null;
        Class<?> type = o.getClass();
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
            StringBuilder builder = new StringBuilder();
            for (Object el : (Collection<?>) o) {
                if (appendSP) {
                    builder.append(", ");
                } else {
                    appendSP = true;
                }
                builder.append(toString(el));
            }
            return builder.toString();
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
    protected boolean isNull(String key) {
        return valuesMap.get(key) == null;
    }

    @Override
    public boolean getBool(String key, boolean v) {
        Object o = valuesMap.get(key);
        if (o == null) return v;
        if (o.getClass().equals(Boolean.class)) {
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

