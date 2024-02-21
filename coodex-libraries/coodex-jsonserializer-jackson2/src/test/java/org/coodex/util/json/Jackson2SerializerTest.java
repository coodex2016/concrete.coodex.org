/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.util.json;

import org.coodex.util.JSONSerializer;
import org.coodex.util.JSONSerializerTestCase;
import org.coodex.util.Valuable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

enum TestNoneValuable {
    A, B, C
}

enum IntValuable implements Valuable.Int {
    A(13), B(14), C(15);
    private final int value;

    IntValuable(int value) {this.value = value;}

    @Override
    public Integer getValue() {
        return value;
    }
}

enum StringValuable implements Valuable<String> {
    D("tt1"), E("tt2"), F("tt3");

    private final String value;

    StringValuable(String value) {this.value = value;}

    @Override
    public String getValue() {
        return value;
    }
}

class POJO {
    public TestNoneValuable a;
    public IntValuable b;
    public StringValuable c;
    public Set<TestNoneValuable> aSet;
    public Set<IntValuable> bSet;
    public Set<StringValuable> cSet;
}

public class Jackson2SerializerTest {
    @Test
    public void test() {
        JSONSerializerTestCase.test(new Jackson2JSONSerializer());
    }

    @Test
    public void test2() {
        Map<String, Object> theMap = new HashMap<>();
        JSONSerializer jsonSerializer = new Jackson2JSONSerializer();
        theMap.put("a", TestNoneValuable.C);
        theMap.put("aSet", Arrays.asList(TestNoneValuable.A, TestNoneValuable.B));
        theMap.put("b", IntValuable.C);
        theMap.put("bSet", Arrays.asList(IntValuable.A, IntValuable.B));
        theMap.put("c", StringValuable.F);
        theMap.put("cSet", Arrays.asList(StringValuable.D, StringValuable.E));
        theMap.put("d", StringValuable.F);
        theMap.put("nullKey", null);
        String s = jsonSerializer.toJson(theMap);
        System.out.println(jsonSerializer.toJson(theMap));

        POJO pojo = jsonSerializer.parse(s, POJO.class);
        Assertions.assertEquals(TestNoneValuable.C,pojo.a);
        Assertions.assertEquals(IntValuable.C,pojo.b);
        Assertions.assertEquals(StringValuable.F,pojo.c);
        Assertions.assertArrayEquals(
                pojo.aSet.stream().sorted(Comparator.comparing(TestNoneValuable::name)).toArray(),
                new TestNoneValuable[]{TestNoneValuable.A, TestNoneValuable.B}
        );
        Assertions.assertArrayEquals(
                pojo.bSet.stream().sorted(Comparator.comparing(IntValuable::name)).toArray(),
                new IntValuable[]{IntValuable.A, IntValuable.B}
        );
        Assertions.assertArrayEquals(
                pojo.cSet.stream().sorted(Comparator.comparing(StringValuable::name)).toArray(),
                new StringValuable[]{StringValuable.D, StringValuable.E}
        );

        System.out.println(jsonSerializer.toJson(pojo));

    }
}

