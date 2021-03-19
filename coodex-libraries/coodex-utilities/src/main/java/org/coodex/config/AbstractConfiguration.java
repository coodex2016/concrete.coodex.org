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

package org.coodex.config;

import org.coodex.util.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractConfiguration implements Configuration {

    @Override
    public <T> T getValue(String key, Supplier<T> defaultValueSupplier, String... namespace) {
        String strValue = get(key, namespace);
        if (strValue == null) return defaultValueSupplier.get();
        return Common.to(strValue, defaultValueSupplier.get());
    }

    @Override
    public <T> T getValue(String key, T defaultValue, String... namespace) {
        return Common.to(get(key, namespace), defaultValue);
    }


    @Override
    public String get(String key, String... namespaces) {
        List<String> ns = toList(namespaces);
        return search(key,
                ns == null || ns.size() == 0 ? null : ns,
                ns == null ? -1 : ns.size());
    }


    protected List<String> toList(String... namespaces) {
        if (namespaces == null) return null;
        List<String> list = new ArrayList<>();
        for (String namespace : namespaces) {
            if (!Common.isBlank(namespace)) {
                list.add(namespace);
            }
        }
        return list;
    }

    private List<String> buildKeys(String key, List<String> namespaces, int deep) {
        List<String> keys = new ArrayList<>();
        if (namespaces != null) {
            for (int i = deep - 1; i < namespaces.size(); i++) {
                StringBuilder temp = new StringBuilder();
                for (int j = i + 1; j < namespaces.size(); j++) {
                    temp.append(namespaces.get(deep - 1 + j - i)).append(".");
                }
                keys.add(temp + key);
            }
        } else {
            keys.add(key);
        }
        return keys;
    }

    private String buildNamespace(List<String> namespaces, int len) {
        if (len == 0)
            return null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0)
                builder.append('.');
            builder.append(namespaces.get(i));
        }
        return builder.toString();
    }


    protected String search(String key, List<String> namespaces, int deep) {
        if (deep == -1) return null;

        String namespace = buildNamespace(namespaces, deep);
        List<String> keys = buildKeys(key, namespaces, deep);

        String value = search(namespace, keys);

        return value == null ? search(key, namespaces, deep - 1) : value;
    }

    protected abstract String search(String namespace, List<String> keys);

//    @Override
//    public String[] getArray(String key, List<String> namespace) {
//        return getArray(key, (String[]) null, namespace);
//    }
//
//    @Override
//    public String[] getArray(String key, String delim, List<String> namespace) {
//        return getArray(key, delim, null, namespace);
//    }
//
//    @Override
//    public String[] getArray(String key, String[] defaultValue, List<String> namespace) {
//        return getArray(key, ",", defaultValue, namespace);
//    }
//
//    @Override
//    public String[] getArray(String key, String delim, String[] defaultValue, List<String> namespace) {
//        return Common.toArray(get(key, namespace), delim, defaultValue);
//    }
}
