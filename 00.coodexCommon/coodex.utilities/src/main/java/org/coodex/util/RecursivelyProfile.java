/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * 可递归的配置
 * 例如
 * <pre>
 * getString("a.b.c.d..e", "key")
 * 会依次检索：
 * a.b.c.d.e.key
 * a.b.c.d.key
 * a.b.c.key
 * a.b.key
 * a.key
 * key
 * </pre>
 * <p>
 * Created by davidoff shen on 2017-05-03.
 */
public class RecursivelyProfile {

    private final StringMap profile;

    public RecursivelyProfile(Profile profile) {
        this.profile = profile;
    }

    public RecursivelyProfile(final Properties properties) {
        this.profile = new StringMap() {
            @Override
            public String getString(String key) {
                return getString(key, null);
            }

            @Override
            public String getString(String key, String defaultValue) {
                return properties == null ? defaultValue : properties.getProperty(key, defaultValue);
            }
        };
    }

    public String getString(String namespace, String key, String value) {
        String result = getString(namespace, key);
        return result == null ? value : result;
    }

    public String getString(String namespace, String key) {
        Stack<String> toSearch = new Stack<String>();
        toSearch.push(key);
        if (!Common.isBlank(namespace)) {
            StringTokenizer stringTokenizer = new StringTokenizer(namespace, ".", false);
            StringBuilder builder = new StringBuilder();
            while (stringTokenizer.hasMoreTokens()) {
                String token = stringTokenizer.nextToken();
                if (Common.isBlank(token)) continue;
                if (builder.length() > 0) {
                    builder.append(".");
                }
                builder.append(token);
                toSearch.push(builder.toString() + "." + key);
            }
        }
        String result = null;
        while (toSearch.size() > 0) {
            String search = toSearch.pop();
            result = profile.getString(search);
            if (result != null) break;
        }
        return result;
    }

//    public static void main(String [] args){
//        RecursivelyProfile profile = new RecursivelyProfile(Profile.getProfile("abcd.properties"));
//        profile.getString("a.b.c.d..e...", "key");
//    }

}
