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
import org.coodex.mock.ext.*;

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
                        Mocker.mock(Pojo.class, Pojo.class.getAnnotations()), // <--放到上下文
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

        @Coordinates
        String testInject() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Mock.Assignation(Pojo3rd.class)
    @interface Pojo3rdCase2 {

        MobilePhoneNum vehicleNum() default @MobilePhoneNum;
    }


    public interface Pojo3rd {

        String getVehicleNum();

    }


    static class PojoAdd{
        @Mock.Number("[0, 100)")
        public int x1;
        @Mock.Number("[0, 100)")
        public int x2;

        @Mock.Relation(dependencies = {"x1", "x2"}, strategy = "add")
        public int sum;
    }

    @Mock.Depth(1)
    @Pojo3rdCase2()
    static class Pojo {
        @Mock.Dimension(size = 20, ordered = true)
        @Mock.Sequence(name = "timestamp", factory = TimestampSequenceFactory.class)
        @Mock.Inject("timestamp")
        @TimestampSequenceFactory.Interval(interval = 1, timeUnit = Calendar.HOUR)
        public Set<String> timestamp;
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
        @FullName
        public String name;
        @IdCard
        public String idCard;

        @Mock.Dimension(size = 4)
        @IpAddress
        public int[][] ip;
        @IpAddress(type = IpAddress.Type.MAC)
        public String mac;
        @IpAddress(type = IpAddress.Type.TPV6)
        public String ipV6;
        @IpAddress
        public String ipV4;
        @IpAddress(type = IpAddress.Type.MAC)
        public Integer[] ipInteger;
        @IpAddress
        public byte[] ip_byte;
        @IpAddress
        public Byte[] ipByte;

//        @Mock.Dimension(size = 50)
        @Setting1
        @Mock.Inject("testInject")
        public double[][] coordinates;

        @Coordinates
        public double longitude;
        @Coordinates(dimension = Coordinates.Dimension.LATITUDE)
        public double latitude;
        @Coordinates
        public Coordinates.Value coordinatesValue;

        public Pojo3rd pojo3rd;

        private String string;

        public String getString() {
            return string;
        }

    }


}
