/*
 * Copyright (c) 2016 - 2024 coodex.org (jujus.shen@126.com)
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

import org.coodex.functional.Function;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Objects;

public class FailureRate {

    public static final int SCALE = 1000000;
    public static int MAX = 7200;
    public static boolean cache = false;
    static SingletonMap<Integer, BigDecimal> max_n = SingletonMap.<Integer, BigDecimal>builder()
            .function(new Function<Integer, BigDecimal>() {
                @Override
                public BigDecimal apply(Integer i) {
                    return BigDecimal.valueOf(MAX).pow(i);
                }
            })
            .build();
    static SingletonMap<Integer, BigDecimal> max_1_n = SingletonMap.<Integer, BigDecimal>builder()
            .function(new Function<Integer, BigDecimal>() {
                @Override
                public BigDecimal apply(Integer i) {
                    return BigDecimal.valueOf(MAX - 1).pow(i);
                }
            })
            .build();
    static SingletonMap<Integer, BigDecimal> f_n = SingletonMap.<Integer, BigDecimal>builder()
            .function(new Function<Integer, BigDecimal>() {
                @Override
                public BigDecimal apply(Integer n) {
                    BigDecimal v = new BigDecimal(1);
                    if (n <= 1) return v;
                    for (int i = 2; i <= n; i++) {
                        v = v.multiply(BigDecimal.valueOf(i));
                    }
                    return v;
                }
            })
            .build();
    public static SingletonMap<Key, BigDecimal> map_c = SingletonMap.<Key, BigDecimal>builder()
            .function(new Function<Key, BigDecimal>() {
                @Override
                public BigDecimal apply(Key key) {
                    return _c(key.x, key.y);
                }
            }).build();

//    static BigDecimal p(int n, int i) {
//        BigDecimal v = new BigDecimal(1);
//        for (int x = 0; x < i; x++) {
//            v = v.multiply(BigDecimal.valueOf(n));
//            n--;
//        }
//        return v;
//    }

    static BigDecimal _c(int n, int i) {
        return f_n.get(n).divide(
                f_n.get(i).multiply(f_n.get(n - i)), SCALE, RoundingMode.HALF_UP
        );
//        int halfN = n / 2;
//        if (i > halfN) i = n - i;
//        if (i == 1) return BigDecimal.valueOf(n);
//        return p(n, i).divide(p(i, i), SCALE, RoundingMode.HALF_UP);
    }

    static BigDecimal c(int n, int i) {
        return cache ? map_c.get(new Key(n, i)) : _c(n, i);
    }

    /**
     * f(n,t)=
     * \begin{cases}
     * & \text{1} & \text{ if } n>MAX \text{ or } t = 0 \\
     * & \sum_{i= 2}^{n} \frac{C_{n}^{i} （MAX-1）^{n-i}}{MAX^{n}} f(i,t-1)  & \text{ if } n<=MAX \text {and } t > 0
     * \end{cases}
     *
     * @param n 出现冲突的节点数
     * @param t 尝试解决冲突的轮次
     * @return 依然存在任何冲突的几率
     */
    static BigDecimal _f(int n, int t) {
        if (t == 0 || n > MAX) return BigDecimal.valueOf(1);

        BigDecimal x = BigDecimal.valueOf(0);
        for (int i = 2; i <= n; i++) {
            x = x.add(
                    c(n, i)
                            .multiply(max_1_n.get(n - i))
                            .divide(max_n.get(n), SCALE, RoundingMode.HALF_UP)
                            .multiply(f(i, t - 1))
            );
        }
//        return x;
//        BigDecimal g = BigDecimal.valueOf(MAX).pow(n);
//        BigDecimal g= p(MAX, MAX);
//        x = x.divide(g, SCALE, RoundingMode.HALF_UP);
//        if (x.longValue() >= 1) return BigDecimal.valueOf(1);
        return x;
    }

    static BigDecimal f(int n, int t) {
        return map_f.get(new Key(n, t));
    }

    static void pre_hot(int n) {
        for (int i = 2; i < Math.max(n, MAX); i++) {
            if (i <= n) f_n.get(i);

            max_1_n.get(i);
            max_n.get(i);
        }
//        long start = System.currentTimeMillis();
//        for (int i = 2; i < n; i++) {
//            map_f.get(new Key(i, 1));
//            if (i % 10 == 0) {
//                long now = System.currentTimeMillis();
//                System.out.println("caching to: " + i + " used: " + (now - start));
//            }
//        }
    }

    public static void main(String[] args) {
//        System.out.println(p(6,2));
//        System.out.println(p(2,2));
//        System.out.println(c(6,2));
//        System.out.println(c(6,4));
//        System.out.println(c(7,2));
//        System.out.println(c(7,5));
        NumberFormat f = NumberFormat.getPercentInstance();
        f.setMaximumFractionDigits(30);

//        System.out.println(f.format(f(5, 3)));
        long start = System.currentTimeMillis();
        pre_hot(5000);
        long now = System.currentTimeMillis();
        System.out.printf("pre_hot ok, used %d%n", now - start);
        start = now;
        System.out.println(f.format(f(5000, 1)));
        now = System.currentTimeMillis();
        System.out.printf("f(5000,1) used %d", now - start);
        start = now;
        System.out.println(f.format(f(5000, 2)));
        now = System.currentTimeMillis();
        System.out.printf("f(5000,2) used %d", now - start);

//        System.out.printf(f(2, 1));
//        System.out.printf("%.50f%n", f(3, 1).doubleValue());
//        System.out.printf("%.50f%n", f(4, 1).doubleValue());
//        System.out.printf("%.50f%n", f(4, 2).doubleValue());
//        System.out.printf("%.50f%n", f(3000, 1).doubleValue());
    }

    public static class Key {
        private final int x;
        private final int y;

        public Key(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return x == key.x && y == key.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static final SingletonMap<Key, BigDecimal> map_f = SingletonMap.<Key, BigDecimal>builder()
            .function(new Function<Key, BigDecimal>() {
                @Override
                public BigDecimal apply(Key key) {
                    return _f(key.x, key.y);
                }
            }).build();


}
