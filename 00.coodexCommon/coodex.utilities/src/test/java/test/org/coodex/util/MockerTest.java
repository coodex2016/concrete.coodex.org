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

package test.org.coodex.util;

import com.alibaba.fastjson.JSON;
import org.coodex.pojomocker.MAP;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.pojomocker.Sequence;
import org.coodex.pojomocker.Sequences;
import org.coodex.pojomocker.annotations.INTEGER;
import org.coodex.pojomocker.annotations.STRING;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MockerTest {

    private final static Logger log = LoggerFactory.getLogger(MockerTest.class);
    private A[][] values;
    private String[] strings;
    private Map<String, Integer> map;

    public static void main(String[] args) {
        log.debug(JSON.toJSONString(MockerFacade.mock(MockerTest.class)));
    }

    @Sequences({
            @Sequence(key = "a", sequenceType = TestSequence.class),
            @Sequence(key = "b", sequenceType = Test2Sequence.class)
    })
    public A[][] getValues() {
        return values;
    }

    public void setValues(A[][] values) {
        this.values = values;
    }

    @Sequence(key = "c",sequenceType = Test2Sequence.class)
    @Sequence.Item(key = "c")
    public String[] getStrings() {
        return strings;
    }

    @Sequences({
            @Sequence(key = "a", sequenceType = TestSequence.class),
            @Sequence(key = "b", sequenceType = Test2Sequence.class)
    })
    @MAP(keySeq = "a")
    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(Map<String, Integer> map) {
        this.map = map;
    }

    public static class A {

        @Sequence.Item(key = "a")
        private String name;


        @STRING(range = {"A", "B", "C"})
        private String name2;

        private Integer value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName2() {
            return name2;
        }

        public void setName2(String name2) {
            this.name2 = name2;
        }

        @INTEGER(min = 3, max = 30)
        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}
