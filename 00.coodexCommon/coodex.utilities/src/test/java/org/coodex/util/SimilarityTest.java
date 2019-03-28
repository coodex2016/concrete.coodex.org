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

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SimilarityTest {

    enum WeekDate{
        MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5),
        SATURDAY(6), SUNDAY(7);

        private final int value;
        WeekDate(int v){
            this.value = v;
        }

        public int getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
//        WeekDate date = WeekDate.MONDAY;
//        if(date.getValue() > 5){
//
//        }

        AtomicInteger i;

       Integer i1 = 1;
        Integer i2 = 1;
        Integer i3 = 1000;
        Integer i4 = 1000;
        System.out.println(i1 == i2);
        System.out.println(i3 == i4);
//        String str1 = "鲁A12345";
//
//        System.out.println(Common.similarity(str1,"鲁A12345"));
//        System.out.println(Common.similarity(str1,"鲁A12005"));
//        System.out.println(Common.similarity(str1,"鲁A12345"));
//        System.out.println(Common.similarity(str1,"鲁A12123"));
//        System.out.println(Common.similarity(str1,"鲁A13245"));
//        System.out.println(Common.similarity(str1,"鲁A12445"));
    }
}
