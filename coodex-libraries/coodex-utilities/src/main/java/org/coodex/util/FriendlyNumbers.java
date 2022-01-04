/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FriendlyNumbers {

    public static final FriendlyNumbers THOUSAND_BASE = new FriendlyNumbers(
            new Param().add(1000, "", 1)
                    .add(1000_000, "K", 1000)
                    .add(1000_000_000, "M", 1000_000)
                    .add(1000_000_000_000L, "B", 1000_000_000)
                    .add(1000_000_000_000_000L, "T", 1000_000_000_000L)
    );
    private final Param param;
    private List<ParamItem> items = null;

    public FriendlyNumbers(Param param) {
        this.param = param;
    }

    public static void main(String[] args) {
        Arrays.asList(599, 14599, 19999, 20001894123L, 99999999999L)
                .forEach(l -> {
                    System.out.println("friendly " + l + ": " + THOUSAND_BASE.format(l.longValue()));
                });

    }

    public String format(long value) {
        if (items == null || param.changed) {
            synchronized (this) {
                if (items == null || param.changed) {
                    items = new ArrayList<>(param.paramItems);
                    items.sort(ParamItem::compareTo);
                    param.changed = false;
                }
            }
        }
        long v = Math.abs(value);
        Optional<ParamItem> item = items.stream().filter(paramItem -> paramItem.maxRange > v).findFirst();
        if (!item.isPresent()) {
            return String.valueOf(value);
        }
        ParamItem paramItem = item.get();
        if (paramItem.divisor == 1) {
            return String.valueOf(value);
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(v);
        bigDecimal = bigDecimal.divide(
                BigDecimal.valueOf(paramItem.divisor),
                param.scale,
                RoundingMode.HALF_UP
        );
        String[] numbers = bigDecimal.toString().split("\\.");
        if (numbers.length == 2) {
            byte[] x = numbers[1].getBytes();
            for (int i = x.length - 1; i >= 0; i--) {
                if (x[i] != '0') {
                    numbers[0] += "." + new String(x, 0, i + 1, StandardCharsets.UTF_8);
                    break;
                }
            }
        }
        return numbers[0] + paramItem.units;
    }

    static class ParamItem implements Comparable<ParamItem> {
        private final long maxRange;
        private final String units;
        private final long divisor;

        ParamItem(long maxRange, String units, long divisor) {
            if (divisor == 0) throw new IllegalArgumentException("divisor not be zero.");
            this.maxRange = Math.abs(maxRange);
            this.units = units;
            this.divisor = Math.abs(divisor);
        }

        private int toInt(long l) {
            return l == 0 ? 0 : l > 0 ? 1 : -1;
        }

        @Override
        public int compareTo(ParamItem o) {
            return o == null ? 1 : toInt(this.maxRange - o.maxRange);
        }
    }

    public static class Param {
        private final int scale;
        private boolean changed = true;
        private List<ParamItem> paramItems = new ArrayList<>();

        public Param() {
            this(1);
        }

        public Param(int scale) {
            this.scale = Math.abs(scale);
        }

        public Param add(long maxRange, String units, long divisor) {
            paramItems.add(new ParamItem(maxRange, units, divisor));
            this.changed = true;
            return this;
        }
    }


}
