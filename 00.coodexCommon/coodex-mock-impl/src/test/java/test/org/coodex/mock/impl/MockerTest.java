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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class MockerTest {

    public static void main(String[] args) {
        System.out.println(
                JSON.toJSONString(
                        Mocker.mock(Pojo.class),
                        SerializerFeature.PrettyFormat
                )
        );
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
        public Boolean booleanValue;
        public Pojo pojo;

        @Mock.String(range = {"男","女"})
        @Mock.Number("[60,80]")
        public Map<String, Integer> scores;
    }
}
