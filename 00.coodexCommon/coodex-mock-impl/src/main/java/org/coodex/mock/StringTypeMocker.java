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

package org.coodex.mock;

import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.coodex.mock.CharTypeMocker.getRange;

public class StringTypeMocker extends AbstractTypeMocker<Mock.String> {

    private final static Logger log = LoggerFactory.getLogger(StringTypeMocker.class);

    private static Class[] SUPPORTED_CLASSES = new Class[]{
            String.class
    };

    @Override
    protected Class[] getSupportedClasses() {
        return SUPPORTED_CLASSES;
    }

    @Override
    protected boolean accept(Mock.String annotation) {
        return true;
    }

    @Override
    public Object mock(Mock.String mockAnnotation, Type targetType) {
        return getStringRange(mockAnnotation).random();
    }

    private StringRange getStringRange(Mock.String stringAnnotation) {
        StringRange range = null;
        if (stringAnnotation != null) {
            range = loadFromResource(stringAnnotation);

            if (range == null && stringAnnotation.range().length > 0) {
                range = new ArrayStringRange(Arrays.asList(stringAnnotation.range()));
            }

            if (range == null && stringAnnotation.charCodeSets().length > 0) {
                range = new CharRangeBasedStringRange(
                        stringAnnotation.minLength(),
                        stringAnnotation.maxLength(),
                        new CharTypeMocker.CodePointSetCharRange(stringAnnotation.charCodeSets(), false)
                );
            }
        }

        if (range == null) {
            range = new CharRangeBasedStringRange(5, 10, new CharTypeMocker.ArrayCharRange(
                    getRange("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", false)
            ));
        }

        return range;
    }

    private StringRange loadFromResource(Mock.String stringAnnotation) {
        if (!Common.isBlank(stringAnnotation.txtResource())) {
            URL url = Common.getResource(stringAnnotation.txtResource());
            if (url != null) {
                List<String> stringList = new ArrayList<String>();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    try {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringList.add(line);
                        }
                    } finally {
                        bufferedReader.close();
                    }
                } catch (Throwable throwable) {
                    log.warn("load resource [{}] error: {}", stringAnnotation, throwable.getLocalizedMessage(), throwable);
                }
                if (stringList.size() > 0) {
                    return new ArrayStringRange(stringList);
                }
            }
        }
        return null;
    }

    interface StringRange {

        String random();

    }

    private static class CharRangeBasedStringRange implements StringRange {
        final int min;
        final int max;
        final CharTypeMocker.CharRange charRange;


        private CharRangeBasedStringRange(int min, int max, CharTypeMocker.CharRange charRange) {
            this.min = Math.max(1, Math.min(min, max));
            this.max = Math.max(1, Math.max(min, max));
            this.charRange = charRange;
        }

        @Override
        public String random() {
            StringBuilder builder = new StringBuilder();
            int len = new Random().nextInt(max - min + 1);
            for (int i = 0; i <= len; i++) {
                builder.append(Character.toChars(charRange.random()));
            }
            return builder.toString();
        }
    }

    private static class ArrayStringRange implements StringRange {
        private List<String> list;

        ArrayStringRange(List<String> strings) {
            list = new ArrayList<String>(strings);
        }

        @Override
        public String random() {
            Random random = new Random();
            return list.get(random.nextInt(list.size()));
        }
    }
}
