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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 某一个一维空间上的线段，可以进行合并，减法操作
 *
 * @param <T> 可比较的
 */
public class Section<T extends Comparable<T>> {

    private T start;
    private T end;

    @SuppressWarnings("WeakerAccess")
    protected Section(T start, T end) {
        if (start == null) {
            throw new NullPointerException("start is null");
        }
        if (end == null) {
            throw new NullPointerException("end is null");
        }
        if (start.compareTo(end) > 0)
            throw new IllegalArgumentException("start > end");

        this.start = cloneObject(start);
        this.end = cloneObject(end);
    }

    /**
     * @param sections 一组线段
     * @param <T>      Comparable
     * @param <S>      Section
     * @return 合并，并排序以后的线段
     */
    public static <T extends Comparable<T>, S extends Section<T>>
    List<S> merge(List<S> sections, Builder<T, S> builder) {
        if (sections == null || sections.size() == 0) return new ArrayList<S>();
        List<S> periodList = new ArrayList<S>(sections);
        Collections.sort(periodList, new Comparator<S>() {
            @Override
            public int compare(S o1, S o2) {
                return o1.getStart().compareTo(o2.getStart());
            }
        });
        List<S> resultList = new ArrayList<S>();
        int index = 0;
        S section;
        do {
            section = builder.create(periodList.get(index).getStart(), periodList.get(index).getEnd());
            index++;
        } while (section.getStart().equals(section.getEnd()) && index < periodList.size());

        if (section.getStart().equals(section.getEnd())) return new ArrayList<S>();

        resultList.add(section);
        for (int i = index; i < periodList.size(); i++) {
            S periodToMerge = periodList.get(i);
            // 包含
            if (section.getEnd().compareTo(periodToMerge.getEnd()) >= 0) {
                continue;
            }
            // 不包含
            if (section.getEnd().compareTo(periodToMerge.getStart()) < 0) {
                if (!periodToMerge.getStart().equals(periodToMerge.getEnd())) {
                    section = builder.create(periodToMerge.getStart(), periodToMerge.getEnd());
                    resultList.add(section);
                }
                continue;
            }
            // 合并
            section.setEnd(periodToMerge.getEnd());
//                    builder.create(section.getStart(), periodToMerge.getEnd());
//            resultList.set(resultList.size() - 1, section);
        }
        return resultList;
    }

    /**
     * 求交集
     *
     * @param s1      组1
     * @param s2      组2
     * @param builder S的builder
     * @param <T>     Comparable
     * @param <S>     Section
     * @return 组1组2的交集
     */
    public static <T extends Comparable<T>, S extends Section<T>>
    List<S> intersect(List<S> s1, List<S> s2, Builder<T, S> builder) {
        if (s1 == null || s1.size() == 0 || s2 == null || s2.size() == 0) return new ArrayList<S>();
        List<S> union = merge(s1, builder);
        union.addAll(merge(s2, builder));
        return sub(
                sub(merge(union, builder),
                        sub(s1, s2, builder), builder),
                sub(s2, s1, builder), builder);

//        List<S> union = merge(s1, builder);
//        union.addAll(merge(s2, builder));
//        union = merge(union, builder);
//        System.out.println("union: " + union);
//        System.out.println("s1 - s2:" + sub(s1, s2, builder));
//        System.out.println("s2 - s1:" + sub(s2, s1, builder));
//        return sub(
//                sub(merge(union, builder),
//                        sub(s1, s2, builder), builder),
//                sub(s2, s1, builder), builder);
    }

    /**
     * @param subtractedList  被减线段集合
     * @param subtractionList 减数线段集合
     * @param builder         S的builder
     * @param <T>             Comparable
     * @param <S>             Section
     * @return 减除以后的线段集合
     */
    public static <T extends Comparable<T>, S extends Section<T>>
    List<S> sub(List<S> subtractedList, List<S> subtractionList, Builder<T, S> builder) {
        // 被减数不得为空
        if (subtractedList == null) throw new RuntimeException("Subtracted Periods must not be null");
        // 被减数0长度，则减任何时间长度均为0
        if (subtractedList.size() == 0) return subtractedList;

        List<S> subtracted = merge(subtractedList, builder);
        if (subtractionList == null || subtractionList.size() == 0)
            return subtracted;
        List<S> subtraction = merge(subtractionList, builder);

        List<S> sections = new ArrayList<S>();
        for (S subtractedSection : subtracted) {
            sections.addAll(sub(subtractedSection, subtraction, builder));
        }
        return sections;
    }

    private static <T extends Comparable<T>, S extends Section<T>>
    List<S> sub(S subtractedSection, List<S> subtraction, Builder<T, S> builder) {
        List<S> result = new ArrayList<S>();
        boolean crossed = false;
        for (S subtractionSection : subtraction) {
            // 减数线段的开始点 >= 被减数线段结束点时，后面不用继续了
            if (subtractionSection.getStart().compareTo(subtractedSection.getEnd()) >= 0)
                break;

            // 减数线段的结束点 <= 被减数线段结束点时，下一个
            if (subtractionSection.getEnd().compareTo(subtractedSection.getStart()) <= 0)
                continue;

            // 一定相交了
            crossed = true;

            // 被减数的开始点 < 减数开始点，增加此部分差值
            if (subtractedSection.getStart().compareTo(subtractionSection.getStart()) < 0) {
                result.add(builder.create(subtractedSection.getStart(), subtractionSection.getStart()));
            }
            // 被减数的结束点 > 减数结束点，继续减
            if (subtractedSection.getEnd().compareTo(subtractionSection.getEnd()) > 0) {
                result.addAll(sub(
                        builder.create(subtractionSection.getEnd(), subtractedSection.getEnd()),
                        subtraction,
                        builder));
                break;
            }
        }

        if (!crossed) {
            result.add(subtractedSection);
        }
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    @SuppressWarnings("WeakerAccess")
    protected T cloneObject(T t) {
        return t;
    }

    public T getStart() {
        return cloneObject(start);
    }

    public T getEnd() {
        return cloneObject(end);
    }

    void setEnd(T end) {
        this.end = cloneObject(end);
    }

    public interface Builder<T extends Comparable<T>, S extends Section<T>> {
        S create(T start, T end);
    }
}
