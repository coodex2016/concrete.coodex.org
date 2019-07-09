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
import org.coodex.concrete.api.mockers.Name;
import org.coodex.concrete.api.mockers.VehicleNum;
import org.coodex.pojomocker.*;
import org.coodex.pojomocker.annotations.INTEGER;
import org.coodex.pojomocker.annotations.STRING;
import org.coodex.pojomocker.sequence.NameSpace;
import org.coodex.pojomocker.sequence.StrDateTimeSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

public class MockerTest {

    @NameSpace("my")
    public static class MyGenerator extends StrDateTimeSequence{}

    private final static Logger log = LoggerFactory.getLogger(MockerTest.class);
    private A[][] values;
    private String[] strings;
    private Map<String, Integer> map;
    private Map<String, String> cars;

    public static void main(String[] args) {
        log.debug(JSON.toJSONString(MockerFacade.mock(MockerTest.class)));
    }

    @Sequences({
            @Sequence(key = "a", sequenceType = TestSequence.class),
            @Sequence(key = "b", sequenceType = Test2Sequence.class)
    })
    @MockerDefTest
    public A[][] getValues() {
        return values;
    }

    public void setValues(A[][] values) {
        this.values = values;
    }

    @NameKey
    @VehicleNumValue
    public Map<String, String> getCars() {
        return cars;
    }

    public void setCars(Map<String, String> cars) {
        this.cars = cars;
    }

    @Sequence(key = "c", sequenceType = MyGenerator.class)
    @Sequence.Item(key = "c")
//    @COLLECTION(size = 2)
    public String[] getStrings() {
        return strings;
    }

    @Sequence(key = "b", sequenceType = Test2Sequence.class)
    @TestKey
    @TestValue
    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(Map<String, Integer> map) {
        this.map = map;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @MAP.Key
    @Sequence.Use(key = "b")
    @interface TestKey {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @MAP.Value
    @INTEGER(min = 10, max = 50)
    @interface TestValue {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @MAP.Key(size = 10)
    @Name
    @interface NameKey {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @MAP.Value
    @VehicleNum
    @interface VehicleNumValue {
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
    @MockerDef
    @interface MockerDefTest {
        @STRING(range = {"A", "b", "c"})
        String str() default "";// str 是模拟器名称

        @INTEGER(min = 10, max = 50)
        int integer() default 0;// integer 是模拟器名称
    }


    public static class A {

        @Sequence.Item(key = "a")
        private String name;


//        @STRING(range = {"A", "B", "C"})
        @MockerRef(name = "str")
        private String name2;

        @MockerRef(name = "integer")
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
