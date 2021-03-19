/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.util;

import org.coodex.util.Section;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class IntSection extends Section<Integer> {
    public static final Builder<Integer, IntSection> builder = IntSection::new;

    protected IntSection(Integer start, Integer end) {
        super(start, end);
    }


    private static void trace(List<IntSection> list) {
        if (list == null || list.size() == 0) {
            System.out.println("empty");
            return;
        }
        StringJoiner joiner = new StringJoiner(", ");
        list.forEach(section -> joiner.add(section.toString()));
        System.out.println(joiner.toString());
    }

    public static void main(String[] args) {
        IntSection section1 = builder.create(0, 3);// 创建一个[0,3]的线段
        IntSection section2 = builder.create(5, 7); //创建一个[5,7]的线段
        IntSection section3 = builder.create(1, 6);// 创建一个[1,6]的线段
        IntSection section4 = builder.create(-1, 10);//创建[-1.10]的线段

        // 合并
        trace(Section.merge(Arrays.asList(section1, section2), builder));
        trace(Section.merge(Arrays.asList(section1, section2,section3), builder));

//        trace(Section.intersect(Arrays.asList(section1, section2)));

    }

    @Override
    public String toString() {
        return "[" + getStart() + ", " + getEnd() + "]";
    }
}
