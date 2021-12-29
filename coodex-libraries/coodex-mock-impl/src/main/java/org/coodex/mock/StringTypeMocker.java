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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.coodex.mock.CharTypeMocker.getRange;
import static org.coodex.mock.Mock.Char.DEFAULT_CHAR_RANGE;
import static org.coodex.mock.Mock.String.*;

public class StringTypeMocker extends AbstractTypeMocker<Mock.String> {

    private final static Logger log = LoggerFactory.getLogger(StringTypeMocker.class);

    private static final Class<?>[] SUPPORTED_CLASSES = new Class<?>[]{
            String.class
    };

    private static final Singleton<StringTypeMocker> instance = Singleton.with(StringTypeMocker::new);

//    public StringTypeMocker() {
//        instance = this;
//    }

    static String mock() {
//        if (instance == null) {
//            instance = new StringTypeMocker();
//        }
        return (String) instance.get().mock(null, null, String.class);
    }

    @Override
    protected Class<?>[] getSupportedClasses() {
        return SUPPORTED_CLASSES;
    }

    @Override
    protected boolean accept(Mock.String annotation) {
        return true;
    }

    @Override
    public String mock(Mock.String mockAnnotation, Type targetType) {
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
                        stringAnnotation.emojiProbability(),
                        new CharTypeMocker.CodePointSetCharRange(stringAnnotation.charCodeSets(), false)
                );
            }
        }

        if (range == null) {
            range = new CharRangeBasedStringRange(
                    stringAnnotation == null ? DEFAULT_MIN_LENGTH : stringAnnotation.minLength(),
                    stringAnnotation == null ? DEFAULT_MAX_LENGTH : stringAnnotation.maxLength(),
                    stringAnnotation == null ? DEFAULT_EMOJI_PROBABILITY : stringAnnotation.emojiProbability(),
                    new CharTypeMocker.ArrayCharRange(
                            getRange(DEFAULT_CHAR_RANGE, false)
                    ));
        }

        return range;
    }

    private StringRange loadFromResource(Mock.String stringAnnotation) {
        if (!Common.isBlank(stringAnnotation.txtResource())) {
            URL url = Common.getResource(stringAnnotation.txtResource());
            if (url != null) {
                List<String> stringList = new ArrayList<>();
                try {
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringList.add(line);
                        }
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
        final float emojiProbability;
        final CharTypeMocker.CharRange charRange;


        private CharRangeBasedStringRange(int min, int max, float emojiProbability, CharTypeMocker.CharRange charRange) {
            this.min = Math.max(1, Math.min(min, max));
            this.max = Math.max(1, Math.max(min, max));
            this.emojiProbability = emojiProbability;
            this.charRange = charRange;
        }

        @Override
        public String random() {
            StringBuilder builder = new StringBuilder();
            int len = new Random().nextInt(max - min + 1) + min;
            for (int i = 0; i <= len; i++) {
                if (emojiProbability > 0 && Math.random() < emojiProbability) {
                    builder.append(EmojiMocker.mock());
                } else {
                    builder.append(Character.toChars(charRange.random()));
                }
            }
            return builder.toString();
        }
    }

    private static class ArrayStringRange implements StringRange {
        private final List<String> list;

        ArrayStringRange(List<String> strings) {
            list = new ArrayList<>(strings);
        }

        @Override
        public String random() {
            Random random = new Random();
            return list.get(random.nextInt(list.size()));
        }
    }

}

class EmojiMocker {
    // 数据来源：https://zh.wikipedia.org/wiki/%E7%B9%AA%E6%96%87%E5%AD%97
    private static final Singleton<EmojiMocker> emojiMockerSingleton = Singleton.with(
            () -> new EmojiMocker()
                    .add(0xa9).add(0xae)
                    .add(0x203c)
                    .add(0x2049)
                    .add(0x2122)
                    .add(0x2139)
                    .add(0x2194, 6)
                    .add(0x21a9, 2)
                    .add(0x231a, 2)
                    .add(0x2328)
                    .add(0x23cf)
                    .add(0x23e9, 11)
                    .add(0x23f8, 3)
                    .add(0x24c2)
                    .add(0x25aa, 2)
                    .add(0x25b6)
                    .add(0x25c0)
                    .add(0x25fb, 4)
                    .add(0x2600, 5).add(0x260e)
                    .add(0x2611).add(0x2614, 2).add(0x2618).add(0x261d)
                    .add(0x2620).add(0x2622, 2).add(0x2626).add(0x262a).add(0x262e, 2)
                    .add(0x2638, 3)
                    .add(0x2640).add(0x2642).add(0x2648, 12)
                    .add(0x2660).add(0x2663).add(0x2665, 2).add(0x2668)
                    .add(0x267b).add(0x267f)
                    .add(0x2692, 6).add(0x2699).add(0x269b, 2)
                    .add(0x26a0, 2).add(0x26aa, 2)
                    .add(0x26b0, 2).add(0x26bd, 2)
                    .add(0x26c4, 2).add(0x26c8).add(0x26ce, 2)
                    .add(0x26d1).add(0x26d3, 2)
                    .add(0x26e9, 2)
                    .add(0x26f0, 6).add(0x26f7, 4).add(0x26fd)
                    .add(0x2702).add(0x2705).add(0x2708, 6).add(0x270f)
                    .add(0x2712).add(0x2714).add(0x2716).add(0x271d)
                    .add(0x2721).add(0x2728)
                    .add(0x2734, 2)
                    .add(0x2744).add(0x2747).add(0x274c).add(0x274e)
                    .add(0x2753, 3).add(0x2757)
                    .add(0x2763, 2)
                    .add(0x2795, 3)
                    .add(0x27a1)
                    .add(0x27b0).add(0x27bf)
                    .add(0x2934, 2)
                    .add(0x2b05, 3)
                    .add(0x2b1b, 2)
                    .add(0x2b50).add(0x2b55)
                    .add(0x3030).add(0x303d)
                    .add(0x3297).add(0x3299)
                    .add(0x1f004)
                    .add(0x1f0cf)
                    .add(0x1f170, 2).add(0x1f1e, 2)
                    .add(0x1f18e)
                    .add(0x1f191, 10)
                    .add(0x1f201, 2)
                    .add(0x1f21a)
                    .add(0x1f22f)
                    .add(0x1f232, 9)
                    .add(0x1f250, 2)
                    .add(0x1f300, 16 * 2 + 2)
                    .add(0x1f324, 7 * 16)
                    .add(0x1f396, 2).add(0x1f399, 3).add(0x1f39e, 5 * 16 + 3)
                    .add(0x1f3f3, 3).add(0x1f3f7, 16 * 16 + 7)
                    .add(0x1f4ff, 16 * 4 - 1)
                    .add(0x1f549, 6)
                    .add(0x1f550, 24)
                    .add(0x1f56f, 2)
                    .add(0x1f573, 8)
                    .add(0x1f587).add(0x1f58a, 4)
                    .add(0x1f590).add(0x1f595, 2)
                    .add(0x1f5a4, 2).add(0x1f5a8)
                    .add(0x1f5b1, 2).add(0x1f5bc)
                    .add(0x1f5c2, 3)
                    .add(0x1f5d1, 3).add(0x1f5dc, 3)
                    .add(0x1f5e1).add(0x1f5e3).add(0x1f5e8).add(0x1f5ef)
                    .add(0x1f5f3).add(0x1f5fa, 16 * 5 - 6)
                    .add(0x1f680, 16 * 4 + 6)
                    .add(0x1f6cc, 8)
                    .add(0x1f6e0, 6).add(0x1f6e9).add(0x1f6eb, 2)
                    .add(0x1f6f0).add(0x1f6f3, 6)
                    .add(0x1f910, 16 * 3 - 5)
                    .add(0x1f93c, 3)
                    .add(0x1f940, 6).add(0x1f947, 6)
                    .add(0x1f950, 16 + 12)
                    .add(0x1f980, 16 + 8)
                    .add(0x1f9c0)
                    .add(0x1f9d0, 16 + 7)

    );
    private final List<java.lang.String> emojiChars = new ArrayList<>();

    private EmojiMocker() {
    }

    public static String mock() {
        return emojiMockerSingleton.get().mockEmojiChar();
    }

    private EmojiMocker add(int code) {
        return add(code, 1);
    }

    private EmojiMocker add(int code, int count) {
        for (int i = 0; i < count; i++) {
            emojiChars.add(emoji(code + i));
        }
        return this;
    }


    private String emoji(int codePoint) {
        return new String(codePoint < 0x10000 ?
                new char[]{(char) codePoint, 0xfe0f} :
                Character.toChars(codePoint));
    }

    private String mockEmojiChar() {
        return emojiChars.get(new Random().nextInt(emojiChars.size()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        emojiChars.forEach(builder::append);
        return "EmojiMocker{" +
                "emojiChars=" + builder +
                '}';
    }
}