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
import org.coodex.util.Singleton;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CharTypeMocker extends AbstractTypeMocker<Mock.Char> {
    static Class<?>[] SUPPORTED_CLASSES = new Class<?>[]{
            char.class, Character.class,
            String.class
    };

    private static Singleton<CharTypeMocker> instance = Singleton.with(CharTypeMocker::new);

//    public CharTypeMocker() {
//        instance = this;
//    }

    static Object mock(Class<?> c) {
//        if (instance == null)
//            instance = new CharTypeMocker();
        return instance.get().mock(null, c);
    }

    static int[] getRange(String strRange, boolean bmp) {
        int[] buf = new int[strRange.length()];
        int charCount = 0;
        for (int i = 0, l = buf.length; i < l; ) {
            int codePoint = strRange.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint >>> 16 == 0 || !bmp) {
                buf[charCount++] = codePoint;
            }
        }

        if (charCount == 0) {
            throw new MockException("none valid char: " + strRange);
        }

        int[] result = new int[charCount];
        System.arraycopy(buf, 0, result, 0, charCount);
        return result;
    }

    @Override
    protected Class<?>[] getSupportedClasses() {
        return SUPPORTED_CLASSES;
    }

    @Override
    protected boolean accept(Mock.Char annotation) {
        return true;
    }

    @Override
    public Object mock(Mock.Char mockAnnotation, Type targetType) {
        CharRange charRange;
        if (mockAnnotation == null || mockAnnotation.value().length == 0) {
            charRange = new ArrayCharRange(getRange(
                    mockAnnotation != null && !Common.isBlank(mockAnnotation.range()) ?
                            mockAnnotation.range() :
                            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                    !String.class.equals(targetType))
            );
        } else {
            charRange = new CodePointSetCharRange(mockAnnotation.value(), !String.class.equals(targetType));
        }

        return to(charRange.random(), targetType);
    }

    private Object to(int codePoint, Type type) {
        if (String.class.equals(type)) {
            return new String(Character.toChars(codePoint));
        } else {
            return (char) codePoint;
        }
    }

    interface CharRange {
        int random();
    }

    static class ArrayCharRange implements CharRange {
        private final int[] range;

        ArrayCharRange(int[] range) {
            if (range == null || range.length == 0) {
                throw new MockException("char range error.");
            }
            this.range = range;
        }


        @Override
        public int random() {
            return range[new Random().nextInt(range.length)];
        }
    }

    static class CodePointSetCharRange implements CharRange {
        private List<CharCodeSet> list = new ArrayList<>();

        CodePointSetCharRange(CharCodeSet[] sets, boolean bmp) {
            for (CharCodeSet set : sets) {
                if (!bmp || set.isBmp()) {
                    list.add(set);
                }
            }

            if (list.size() == 0) {
                throw new MockException("none valid CharCodeSet.");
            }
        }

        @Override
        public int random() {
            return randomChar(new Random());
        }

        private int randomChar(Random random) {
            CharCodeSet charCodeSet = list.get(random.nextInt(list.size()));
            return charCodeSet.getMin() + random.nextInt(charCodeSet.getCount());
        }
    }

}
