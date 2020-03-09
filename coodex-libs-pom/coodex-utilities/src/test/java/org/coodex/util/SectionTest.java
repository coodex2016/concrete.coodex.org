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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SectionTest {
    public static Section.Builder<Integer, IntSection> BUILDER = new Section.Builder<Integer, IntSection>() {
        @Override
        public IntSection create(Integer start, Integer end) {
            return new IntSection(start, end);
        }
    };

    private static void list(List<IntSection> list, String str) {
        System.out.println("===== " + str);
        for (IntSection intSection : list) {
            System.out.println(intSection);
        }
    }

    private static List<IntSection> randomSections() {
        Random random = new Random();
        List<IntSection> intSections = new ArrayList<IntSection>();
        int count = random.nextInt(30) + 5;
        for (int i = 0; i < count; i++) {
            int start = random.nextInt();
            int end = start + random.nextInt();
            if (start > end) {
                int x = start;
                start = end;
                end = x;
            }
            intSections.add(BUILDER.create(start, end));
        }
        return intSections;
    }

    private static void run(Runnable runnable, String label) {
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println(
                label + " used " + (System.currentTimeMillis() - start) + " ms"
        );
    }

    public static void main(String[] args) {
        int count = 100000;
        final List<List<IntSection>> lists = new ArrayList<List<IntSection>>();
        for (int i = 0; i < count; i++) {
            lists.add(randomSections());
        }

        run(new Runnable() {
            @Override
            public void run() {
                for (List<IntSection> intSections : lists) {
                    Section.merge(intSections, BUILDER);
                }
            }
        }, "merge " + count + " times");

        run(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < lists.size(); i++) {
                    List<IntSection> subtracted = i == 0 ? lists.get(lists.size() - 1) : lists.get(i - 1);
                    List<IntSection> subtraction = lists.get(i);
                    Section.sub(subtracted, subtraction, BUILDER);
                }
            }
        }, "sub " + count + " times");

        run(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < lists.size(); i++) {
                    List<IntSection> subtracted = i == 0 ? lists.get(lists.size() - 1) : lists.get(i - 1);
                    List<IntSection> subtraction = lists.get(i);
                    Section.intersect(subtracted, subtraction, BUILDER);
                }
            }
        }, "intersect " + count + " times");


        List<IntSection> subtraction = Arrays.asList(
                new IntSection(-1, 3),
                new IntSection(9, 20),
                new IntSection(6, 9),
                new IntSection(24, 90)
        );

        list(Section.sub(Arrays.asList(new IntSection(1, 100)), subtraction, BUILDER), "sub1");

        list(Section.sub(Arrays.asList(new IntSection(-10, 25)), subtraction, BUILDER), "sub2");

        list(Section.sub(Arrays.asList(
                new IntSection(-10, 25),
                new IntSection(69, 79),
                new IntSection(80, 100)
                ),
                subtraction, BUILDER), "sub3");

        list(Section.merge(Arrays.asList(
                new IntSection(1, 3),
                new IntSection(3, 5),
                new IntSection(8, 8)
        ), BUILDER), "merge1");

        list(Section.merge(Arrays.asList(
                new IntSection(8, 8)
        ), BUILDER), "merge2");

        list(Section.sub(
                Arrays.asList(new IntSection(1, 3), new IntSection(6, 10)),
                Arrays.asList(new IntSection(2, 4), new IntSection(7, 8)), BUILDER), "sub4");

        list(Section.sub(
                Arrays.asList(new IntSection(2, 4)),
                Arrays.asList(new IntSection(2, 3)),
                BUILDER), "sub5");

        list(Section.sub(
                Arrays.asList(new IntSection(1, 3), new IntSection(6, 10)),
                Arrays.asList(new IntSection(2, 4), new IntSection(7, 8)), BUILDER), "sub6");

        list(Section.intersect(
                Arrays.asList(new IntSection(1, 3), new IntSection(6, 10)),
                Arrays.asList(new IntSection(2, 4), new IntSection(7, 8)), BUILDER), "intersect");
    }

    public static class IntSection extends Section<Integer> {
        IntSection(Integer start, Integer end) {
            super(start, end);
        }
    }
}
