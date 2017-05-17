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

package org.coodex.pojomocker.mockers;

import org.coodex.pojomocker.Mocker;
import org.coodex.pojomocker.annotations.STRING;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class DefaultStringMocker implements Mocker<STRING> {

    static char[] getDefaultRange() {
        return Profile.getProfile("mock.properties").getString("default.string.range", DefaultCharMocker.DEFAULT_RANGE_STR).toCharArray();
    }

    @Override
    public boolean accept(STRING param) {
        return param != null;
    }

    @Override
    public Object mock(STRING mockAnnotation, Class clazz) {

        if (!Common.isBlank(mockAnnotation.txt())) {
            String[] range = loadFromTxt(mockAnnotation.txt());
            if (range != null && range.length > 0)
                return Common.random(range);
        }

        if (mockAnnotation.range() != null && mockAnnotation.range().length > 0) {
            return Common.random(mockAnnotation.range());
        }
        int min = Math.max(0, mockAnnotation.minLen());
        int max = Math.max(min, mockAnnotation.maxLen());
        char[] range = getDefaultRange();
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = min == max ? min : Common.random(min, max); i < len; i++) {
            builder.append(range[Common.random(range.length - 1)]);
        }

        return builder.toString();
    }

    private String[] loadFromTxt(String txt) {
        try {
            URL url = Common.getResource(Common.trim(txt, '/', '\\'),
                    DefaultStringMocker.class.getClassLoader());
            BufferedReader reader = null;
            if (url == null) {
                File f = new File(txt);
                if (f.exists())
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("utf8")));
            } else {
                reader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("utf8")));
            }

            if (reader != null) {
                String line;
                List<String> stringList = new ArrayList<String>();
                while ((line = reader.readLine()) != null) {
                    if (!Common.isBlank(line) && !line.trim().startsWith("#"))
                        stringList.add(line);
                }
                if (stringList.size() > 0)
                    return stringList.toArray(new String[0]);
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }
}
