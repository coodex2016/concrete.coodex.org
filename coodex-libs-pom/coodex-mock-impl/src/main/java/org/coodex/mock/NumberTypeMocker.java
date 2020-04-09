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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.coodex.mock.Mock.Number.DEFAULT_DIGITS;
import static org.coodex.mock.Mock.Number.DEFAULT_RANGE;
import static org.coodex.util.Common.cast;

/**
 * 按照{@link Mock.Number}约定的一个模拟器实现
 *
 * @author Davidoff
 */
public class NumberTypeMocker extends AbstractTypeMocker<Mock.Number> {

    static Class<?>[] SUPPORTED = new Class<?>[]{
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
    };
    private static char[] IGNORE = " \t\r\n".toCharArray();
    private static char[] RANGE_START = "[(".toCharArray();
    private static char[] RANGE_END = ")]".toCharArray();
    private static char[] DELIMITER = ",".toCharArray();
    private static char[] LEFT_BRACKETS_INCLUDE = "[".toCharArray();
    private static char[] RIGHT_BRACKETS_INCLUDE = "]".toCharArray();

    private static Singleton<NumberTypeMocker> instance = Singleton.with(NumberTypeMocker::new);

//    public NumberTypeMocker() {
//        instance = this;
//    }

    static Object mock(Class<?> c) {
//        if (instance == null)
//            instance = new NumberTypeMocker();
        return instance.get().mock(null, null, c);
    }

    private static boolean inArray(char ch, char[] chars) {
        for (char chInArray : chars) {
            if (ch == chInArray) return true;
        }
        return false;
    }

    private static List<String> split(String range) {
        List<String> list = new ArrayList<>();
        StringBuilder builder = null;
        for (char ch : range.toCharArray()) {
            if (inArray(ch, DELIMITER)) {
                if (builder == null) {
                    throw new MockException("invalid range: " + range);
                }
                String v = builder.toString().trim();
                if (!Common.isBlank(v)) {
                    list.add(v);
                }
                builder = null;
                continue;
            }

            if (inArray(ch, IGNORE)) {
                if (builder != null) {
                    builder.append(ch);
                }
                continue;
            }

            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(ch);
        }
        if (builder != null) {
            String v = builder.toString().trim();
            if (!Common.isBlank(v)) {
                list.add(v);
            }
        }
        return list;
    }

//    public static void main(String[] args) {
//        String[] testStr = {
//                "[min,0], 15, [100,MAX], max",
//                "[0,1]",
//                "(-100, 100)",
//                "(-100, -5)",
//                "[-1,1]"};
//
//        for (int i = 0; i < 2000; i++)
//            for (String s : testStr) {
//                getAlternative(s, Float.class).mock();
//            }
//
//        for (String s : testStr) {
//            System.out.println(getAlternative(s, short.class).mock());
//        }
//    }

    private static <T> Range<T> buildRange(String left, String right, Class<T> c) {
        if (!inArray(left.charAt(0), RANGE_START)) {
            throw new MockException("invalid range: " + left);
        }
        char rightLast = right.charAt(right.length() - 1);
        if (!inArray(rightLast, RANGE_END)) {
            throw new MockException("invalid range: " + right);
        }
        boolean includeMin = inArray(left.charAt(0), LEFT_BRACKETS_INCLUDE);
        boolean includeMax = inArray(rightLast, RIGHT_BRACKETS_INCLUDE);
        String min = left.substring(1);
        String max = right.substring(0, right.length() - 1);
        if (Byte.class.equals(c) || byte.class.equals(c)) {
            return cast(new ByteRange(includeMin, min, max, includeMax));
        } else if (Short.class.equals(c) || short.class.equals(c)) {
            return cast(new ShortRange(includeMin, min, max, includeMax));
        } else if (Integer.class.equals(c) || int.class.equals(c)) {
            return cast(new IntegerRange(includeMin, min, max, includeMax));
        } else if (Long.class.equals(c) || long.class.equals(c)) {
            return cast(new LongRange(includeMin, min, max, includeMax));
        } else if (Float.class.equals(c) || float.class.equals(c)) {
            return cast(new FloatRange(includeMin, min, max, includeMax));
        } else if (Double.class.equals(c) || double.class.equals(c)) {
            return cast(new DoubleRange(includeMin, min, max, includeMax));
        } else {
            return null;
        }
    }

    private static <T> Alternative<T> getAlternative(String range, Class<T> clz) {
        List<String> list = split(range);
        Merge<T> merge = new Merge<>();
        for (int i = 0, len = list.size(); i < len; ) {
            String s = list.get(i);
            if (inArray(s.charAt(0), RANGE_START)) {
                merge.add(buildRange(s, list.get(i + 1), clz));
                i += 2;
            } else {
                merge.add(buildSingle(s, clz));
                i++;
            }
        }
        return merge;
    }

    private static <T> Alternative<T> buildSingle(String s, Class<T> c) {
        if (Byte.class.equals(c) || byte.class.equals(c)) {
            return cast(new ByteSingle(s));
        } else if (Short.class.equals(c) || short.class.equals(c)) {
            return cast(new ShortSingle(s));
        } else if (Integer.class.equals(c) || int.class.equals(c)) {
            return cast(new IntegerSingle(s));
        } else if (Long.class.equals(c) || long.class.equals(c)) {
            return cast(new LongSingle(s));
        } else if (Float.class.equals(c) || float.class.equals(c)) {
            return cast(new FloatSingle(s));
        } else if (Double.class.equals(c) || double.class.equals(c)) {
            return cast(new DoubleSingle(s));
        } else {
            return null;
        }
    }

    private static long parseNumber(String str, int bits) {
        switch (bits) {
            case 8:
                return Byte.parseByte(str);
            case 16:
                return Short.parseShort(str);
            case 32:
                return Integer.parseInt(str);
            default:
                return Long.parseLong(str);
        }
    }

    private static long parseHex(String str, int bits) {
        switch (bits) {
            case 8:
                return Byte.parseByte(str, 16);
            case 16:
                return Short.parseShort(str, 16);
            case 32:
                return Integer.parseInt(str, 16);
            default:
                return Long.parseLong(str, 16);
        }
    }

    private static long getMax(int bits) {
        switch (bits) {
            case 8:
                return Byte.MAX_VALUE;
            case 16:
                return Short.MAX_VALUE;
            case 32:
                return Integer.MAX_VALUE;
            default:
                return Long.MAX_VALUE;
        }
    }

    private static long getMin(int bits) {
        switch (bits) {
            case 8:
                return Byte.MIN_VALUE;
            case 16:
                return Short.MIN_VALUE;
            case 32:
                return Integer.MIN_VALUE;
            default:
                return Long.MIN_VALUE;
        }
    }

//    public static <T> T mock(String rangeStr, Class<T> tClass) {
//        String DEFAULT_RANGE = "[min, max]";
//        String range = (rangeStr == null || Common.isBlank(rangeStr.trim())) ? DEFAULT_RANGE : rangeStr;
//        Class c = getClassFromType(tClass);
//        int index = Common.findInArray(c, SUPPORTED);
//        if (index < 0) {
//            throw new MockException(tClass + " not supported.");
//        }
//        //noinspection unchecked
//        return (T) getAlternative(range, SUPPORTED[index]);
//    }

    public static Object mock(Type targetType, String range, int digits) {
        Class<?> c = getClassFromType(targetType);
        int index = Common.findInArray(c, SUPPORTED);

        return round(index, getAlternative(range, SUPPORTED[index]).mock(), digits);
    }

    private static Object round(int i, Object value, int digits) {
        if (digits == -1) return value;

        final BigDecimal bigDecimal = new BigDecimal(value.toString()).setScale(digits, BigDecimal.ROUND_HALF_UP);
        switch (i) {
            case 8:// float
            case 9:
                return bigDecimal.floatValue();
            case 10:// double
            case 11:
                return bigDecimal.doubleValue();
            default:
                return value;
        }

    }

    @Override
    protected Class<?>[] getSupportedClasses() {
        return SUPPORTED;
    }

    @Override
    protected boolean accept(Mock.Number annotation) {
        return true;
    }

    @Override
    public Object mock(Mock.Number mockAnnotation, Type targetType) {
        String range = (mockAnnotation == null || Common.isBlank(mockAnnotation.value().trim())) ?
                DEFAULT_RANGE : mockAnnotation.value();
        int digits = mockAnnotation == null ? DEFAULT_DIGITS : mockAnnotation.digits();

        return mock(targetType, range, digits);
    }


    private interface Alternative<T> {
        int weight();

        T mock();
    }

    private static class Merge<T> implements Alternative<T> {

        private List<Alternative<T>> alternatives = new ArrayList<>();
        private int weight = 0;

        void add(Alternative<T> alternative) {
            alternatives.add(alternative);
            weight += alternative.weight();
        }

        @Override
        public int weight() {
            return weight;
        }

        @Override
        public T mock() {
            int random = weight > 1 ? new Random().nextInt(weight) : 0;
            for (Alternative<T> alternative : alternatives) {
                random -= alternative.weight();
                if (random < 0) {
                    return alternative.mock();
                }
            }
            throw new MockException("weight error: " + random);
        }


        @Override
        public String toString() {
            return "Merge{" +
                    "alternatives=" + alternatives +
                    ", weight=" + weight +
                    '}';
        }
    }

    private abstract static class Single<T> implements Alternative<T> {
        private T value;
        private String numberStr;

        Single(String valueStr) {
            this.numberStr = valueStr;
            value = parseValue(valueStr);
        }

        abstract T parseValue(String valueStr);

        @Override
        public int weight() {
            return 1;
        }

        @Override
        public T mock() {
            return value;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "value=" + value +
                    ", numberStr='" + numberStr + '\'' +
                    '}';
        }
    }

    private abstract static class AbstractIntSingle<T extends Number> extends Single<T> {
        AbstractIntSingle(String valueStr) {
            super(valueStr);
        }

        abstract int getBits();

        abstract T to(long l);

        @Override
        T parseValue(String valueStr) {
            if ("min".equalsIgnoreCase(valueStr)) {
                return to(getMin(getBits()));
            } else if ("max".equalsIgnoreCase(valueStr)) {
                return to(getMax(getBits()));
            } else if (valueStr.toLowerCase().startsWith("0x")) {
                return to(parseHex(valueStr.substring(2), getBits()));
            } else
                return to(parseNumber(valueStr, getBits()));
        }
    }

    private static class DoubleSingle extends Single<Double> {

        DoubleSingle(String valueStr) {
            super(valueStr);
        }

        @Override
        Double parseValue(String valueStr) {
            if ("min".equalsIgnoreCase(valueStr)) {
                return -Double.MAX_VALUE;
            } else if ("max".equalsIgnoreCase(valueStr)) {
                return Double.MAX_VALUE;
            } else {
                return Double.parseDouble(valueStr);
            }
        }
    }

    private static class FloatSingle extends Single<Float> {

        FloatSingle(String valueStr) {
            super(valueStr);
        }

        @Override
        Float parseValue(String valueStr) {
            if ("min".equalsIgnoreCase(valueStr)) {
                return -Float.MAX_VALUE;
            } else if ("max".equalsIgnoreCase(valueStr)) {
                return Float.MAX_VALUE;
            } else {
                return Float.parseFloat(valueStr);
            }
        }
    }

    private static class LongSingle extends AbstractIntSingle<Long> {

        LongSingle(String valueStr) {
            super(valueStr);
        }

        @Override
        int getBits() {
            return 64;
        }

        @Override
        Long to(long l) {
            return l;
        }
    }

    private static class IntegerSingle extends AbstractIntSingle<Integer> {
        IntegerSingle(String valueStr) {
            super(valueStr);
        }

        @Override
        int getBits() {
            return 32;
        }

        @Override
        Integer to(long l) {
            return (int) l;
        }
    }

    private static class ShortSingle extends AbstractIntSingle<Short> {

        ShortSingle(String valueStr) {
            super(valueStr);
        }

        @Override
        int getBits() {
            return 16;
        }

        @Override
        Short to(long l) {
            return (short) l;
        }
    }

    private static class ByteSingle extends AbstractIntSingle<Byte> {

        ByteSingle(String valueStr) {
            super(valueStr);
        }

        @Override
        int getBits() {
            return 8;
        }

        @Override
        Byte to(long l) {
            return (byte) l;
        }
    }

    private abstract static class Range<T> implements Alternative<T> {

        int weight;
        T min;
        String minStr;
        String maxStr;
        boolean includeMin;
        T max;
        boolean includeMax;

        Range(boolean includeMin, String min, String max, boolean includeMax) {
            this.includeMin = includeMin;
            this.includeMax = includeMax;
            this.maxStr = max;
            this.minStr = min;
            this.min = parseMin(min);
            this.max = parseMax(max);
        }

        abstract T parseMin(String min);

        abstract T parseMax(String max);

        @SuppressWarnings("unused")
        abstract int calcWeight();

        @Override
        public int weight() {
            return weight;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "weight=" + weight + ", def=" +
                    (includeMin ? '[' : '(') + minStr + ", " + maxStr +
                    (includeMax ? ']' : ')') + ", range=" +
                    (includeMin ? '[' : '(') + min + ", " + max +
                    (includeMax ? ']' : ')') + '}';
        }
    }

    private static class DoubleRange extends Range<Double> {

        DoubleRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
            this.weight = calcWeight();
        }

        @Override
        public Double mock() {
            return check(random());
        }

        @Override
        Double parseMin(String min) {
            if (min.toLowerCase().equals("min")) {
                return -Double.MAX_VALUE;
            } else {
                return Double.parseDouble(min);
            }
        }

        @Override
        Double parseMax(String max) {
            if (max.toLowerCase().equals("max")) {
                return Double.MAX_VALUE;
            } else {
                return Double.parseDouble(max);
            }
        }

        private Double random() {
            double x = this.max - this.min;
            double random = Math.random();
            if (Double.isInfinite(x)) {
                double over = random * Double.MAX_VALUE;
                if (Math.random() < 0.5) {
                    over = over * -1.0d;
                }
                if (over > this.max) {
                    return over - this.max + this.min;
                } else if (over < this.min) {
                    return this.max - (this.min - over);
                } else
                    return over;
            } else {
                return this.min + random * x;
            }
        }

        private Double check(Double v) {
            if (v >= this.max || v < this.min)
                throw new MockException("mock error: " + v + ". " + this.toString());
            else
                return v;
        }

        @Override
        int calcWeight() {
            double x = this.max - this.min;
            if (Double.isInfinite(x)) return Mock.Number.MAX_WEIGHT;
            if (this.min + Mock.Number.MAX_WEIGHT > this.max) {

                return Math.max(1, (int) x);
            } else {
                return Mock.Number.MAX_WEIGHT;
            }
        }
    }

    private static class FloatRange extends Range<Float> {
        FloatRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
            this.weight = calcWeight();
        }

        private Float random() {
            float x = this.max - this.min;
            float random = (float) Math.random();
            if (Float.isInfinite(x)) {
                float over = random * Float.MAX_VALUE;
                if (Math.random() < 0.5) {
                    over = over * -1f;
                }
                if (over > this.max) {
                    return over - this.max + this.min;
                } else if (over < this.min) {
                    return this.max - (this.min - over);
                } else
                    return over;
            } else {
                return this.min + random * x;
            }
        }

        private Float check(Float v) {
            if (v >= this.max || v < this.min)
                throw new MockException("mock error: " + v + ". " + this.toString());
            else
                return v;
        }

        @Override
        public Float mock() {
            return check(random());
        }

        @Override
        Float parseMin(String min) {
            if (min.toLowerCase().equals("min")) {
                return -Float.MAX_VALUE;
            } else {
                return Float.parseFloat(min);
            }
        }

        @Override
        Float parseMax(String max) {
            if (max.toLowerCase().equals("max")) {
                return Float.MAX_VALUE;
            } else {
                return Float.parseFloat(max);
            }
        }

        @Override
        int calcWeight() {
            float x = this.max - this.min;
            if (Float.isInfinite(x)) return Mock.Number.MAX_WEIGHT;
            if (this.min + Mock.Number.MAX_WEIGHT > this.max) {

                return Math.max(1, (int) x);
            } else {
                return Mock.Number.MAX_WEIGHT;
            }
        }
    }

    private abstract static class AbstractIntRange<T extends Number> extends Range<T> {
        private long longMin;
        private long longMax;

        AbstractIntRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
            this.longMin = this.min.longValue();
            if (!includeMin) this.longMin++;
            this.longMax = this.max.longValue();
            if (!includeMax) this.longMax--;
            this.weight = calcWeight();
        }

        abstract int getBits();

        @Override
        T parseMin(String min) {
            String s = min.toLowerCase();
            if (s.equals("min"))
                return to(getMin(getBits()));

            return parse(min);
        }


        @Override
        T parseMax(String max) {
            String s = max.toLowerCase();
            if (s.equals("max"))
                return to(getMax(getBits()));
            return parse(max);
        }

        private T parse(String value) {
            long l;
            String temp = value.toLowerCase();
            if (temp.startsWith("0x")) {
                l = parseHex(temp.substring(2), getBits());
            } else {
                l = parseNumber(value, getBits());
            }
            if (l > getMax(getBits()) || l < getMin(getBits())) {
                throw new MockException("out of range: " + value);
            }
            return to(l);
        }


        @Override
        int calcWeight() {
            long x = longMax - longMin + 1;

            if (x <= 0) {
                // 越界
                if (longMin < longMax) {
                    return Mock.Number.MAX_WEIGHT;
                } else {
                    throw new MockException("range error"
                            + (includeMin ? '[' : '(')
                            + minStr + ", " + maxStr
                            + (includeMax ? ']' : ')'));
                }
            } else {
                return (int) Math.min(x, Mock.Number.MAX_WEIGHT);
            }
        }

        private long check(long random) {
            if (random > longMax || random < longMin) {
                throw new MockException("out of range: " + random + ". "
                        + (includeMin ? '[' : '(')
                        + minStr + ", " + maxStr
                        + (includeMax ? ']' : ')'));
            } else
                return random;
        }

        @Override
        public T mock() {
            return to(check(random()));
        }

        private long random() {
            if (longMin == longMax) return longMin;

            long x = longMax - longMin + 1;
            long random = new Random().nextLong();

            // 越界
            if (x <= 0) {
                if (random > longMax) {
                    return longMin + (random - longMax);
                } else if (random < longMin) {
                    return longMax - ((longMin - random));
                } else {
                    return random;
                }
            } else {
                return longMin + Math.abs(random) % x;
            }
        }

        abstract T to(long random);
    }


    private static class LongRange extends AbstractIntRange<Long> {
        LongRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
        }

        @Override
        int getBits() {
            return 64;
        }

        @Override
        Long to(long random) {
            return random;
        }
    }

    private static class IntegerRange extends AbstractIntRange<Integer> {

        IntegerRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
        }

        @Override
        int getBits() {
            return 32;
        }

        @Override
        Integer to(long random) {
            return (int) random;
        }
    }

    private static class ShortRange extends AbstractIntRange<Short> {
        ShortRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
        }

        @Override
        int getBits() {
            return 16;
        }

        @Override
        Short to(long random) {
            return (short) random;
        }
    }

    private static class ByteRange extends AbstractIntRange<Byte> {

        ByteRange(boolean includeMin, String min, String max, boolean includeMax) {
            super(includeMin, min, max, includeMax);
        }

        @Override
        int getBits() {
            return 8;
        }

        @Override
        Byte to(long random) {
            return (byte) random;
        }
    }
}
