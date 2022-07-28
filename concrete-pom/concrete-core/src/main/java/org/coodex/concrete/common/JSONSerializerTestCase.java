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

package org.coodex.concrete.common;

import org.coodex.util.GenericTypeHelper;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JSONSerializerTestCase {

    private static final Logger log = LoggerFactory.getLogger(JSONSerializerTestCase.class);

    public static class ObjTest {
        public int a = 1;
        public String b = "null";
        public String c = "{}";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjTest objTest = (ObjTest) o;
            return a == objTest.a && Objects.equals(b, objTest.b) && Objects.equals(c, objTest.c);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c);
        }
    }

    public static void test(JSONSerializer jsonSerializer) {
        String nullStr = jsonSerializer.toJson(null);
        Assertions.assertEquals(nullStr, "null");
        Assertions.assertNull(jsonSerializer.parse(null,Object.class));
        Assertions.assertNull(jsonSerializer.parse("",Object.class));
        Object o = jsonSerializer.parse(nullStr, Object.class);
        Assertions.assertNull(o);

        String strNullStr = jsonSerializer.toJson("null");
        Assertions.assertEquals(strNullStr, "\"null\"");
        Assertions.assertEquals("null", jsonSerializer.parse(strNullStr, String.class));

        Assertions.assertNull(jsonSerializer.parse(null, Object.class));

        Map<String, Object> map = new HashMap<>();

        map.put("number", 1.0f);
        map.put("numberArray", new int[]{1, 2, 3});
        map.put("string", "str");
        map.put("stringArray", new String[]{"str1", "str2"});
        map.put("null", null);
        map.put("nullArray", new Object[]{null, null});
        Object obj = new ObjTest();
        map.put("object", obj);
        map.put("objectArray", new Object[]{obj, obj});

        String mapJson = jsonSerializer.toJson(map);

        Map<String, Object> stringObjectMap = jsonSerializer.parse(mapJson,
                new GenericTypeHelper.GenericType<Map<String, Object>>() {
                }.getType());

        Float number = jsonSerializer.parse(stringObjectMap.get("number"), Float.class);
        Assertions.assertEquals(number, 1.0f);
        Assertions.assertArrayEquals(
                new int[]{1, 2, 3},
                jsonSerializer.parse(stringObjectMap.get("numberArray"), int[].class)
        );
        Assertions.assertEquals("str", jsonSerializer.parse(stringObjectMap.get("string"), String.class));
        Assertions.assertArrayEquals(
                new String[]{"str1", "str2"},
                jsonSerializer.parse(stringObjectMap.get("stringArray"), String[].class)
        );
        Assertions.assertNull(jsonSerializer.parse(stringObjectMap.get("null"), String.class));
        Assertions.assertArrayEquals(
                new Object[]{null, null},
                jsonSerializer.parse(stringObjectMap.get("nullArray"), Object[].class)
        );
        Assertions.assertEquals(
                obj,
                jsonSerializer.parse(stringObjectMap.get("object"), ObjTest.class)
        );
        Assertions.assertArrayEquals(
                new Object[]{obj, obj},
                jsonSerializer.parse(stringObjectMap.get("objectArray"), ObjTest[].class)
        );

        case2(mapJson, jsonSerializer);

        case2("[1,2,3]", jsonSerializer);

        case2("1", jsonSerializer);

        case2("null", jsonSerializer);

        case2(jsonSerializer.toJson(obj), jsonSerializer);
    }

    private static void case2(String str, JSONSerializer jsonSerializer) {
        Object o = jsonSerializer.parse(str, Object.class);
        log.info(o == null ? "null" : (o.getClass() + ":" + o));
        Assertions.assertEquals(
                str,
                jsonSerializer.toJson(o)
        );
    }
}
