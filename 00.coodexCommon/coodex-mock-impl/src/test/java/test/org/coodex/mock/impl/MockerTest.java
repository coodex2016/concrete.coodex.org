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

package test.org.coodex.mock.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.coodex.mock.Mock;
import org.coodex.mock.Mocker;
import org.coodex.mock.ext.FullName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;
import java.util.Map;

public class MockerTest {

    public static void main(String[] args) {
        System.out.println(
                JSON.toJSONString(
                        Mocker.mock(Pojo.class),
                        SerializerFeature.PrettyFormat
                )
        );
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Mock.Declaration
    @interface Setting1 {
        //方式1
        @FullName
        String testKey() default "";

        //方式2
        Mock.Number testValue() default @Mock.Number;

        @Mock.String(range = {"男", "女"})
        Mock.Number map() default @Mock.Number;
    }


    @Mock.Depth(1)
    static class Pojo {
//        @Mock.Dimension(size = 20, ordered = true)
//        @Mock.Sequence(name = "timestamp", factory = TimestampSequenceFactory.class)
//        @Mock.Inject("timestamp")
//        @TimestampSequenceFactory.Interval(interval = 1, timeUnit = Calendar.HOUR)
//        public Set<String> timestamp;
        @Mock.String(range = {"coodex", "concrete", "真棒!"})
        @Mock.Nullable(probability = 0.5d)
        public String stringValue;
        @Mock.Number("[0,4],9")
        public Integer integerValue;
        @Mock.Number("[-2.0f, 2.0f]")
        public Float floatValue;
        public Pojo pojo;

        @Mock.Dimension(size = 5)
        @Mock.Sequence(name = "map", factory = TimestampSequenceFactory.class)
        @TimestampSequenceFactory.Interval(interval = 1, timeUnit = Calendar.HOUR)
        @Setting1(map = @Mock.Number("[60,80]"))
        @Mock.Key("testKey")
        @Mock.Value("map")
        public Map<String, Integer> scores;
    }


    static class PojoAdd{
        @Mock.Number("[0, 100)")
        public int x1;
        @Mock.Number("[0, 100)")
        public int x2;

        @Mock.Relation(dependencies = {"x1", "x2"}, strategy = "add")
        public int sum;
    }

}
