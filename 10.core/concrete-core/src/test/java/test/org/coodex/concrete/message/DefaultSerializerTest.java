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

package test.org.coodex.concrete.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.coodex.concrete.core.signature.DefaultSignatureSerializer;

import java.util.Map;

public class DefaultSerializerTest {

    public static void main(String[] args) {
        String s = "{\n" +
                "\"recordId\": 99995,\n" +
                "\"state\":{\n" +
                " \"details\":[\n" +
                "  {\n" +
                "   \"fee\":10,\n" +
                "   \"paymentWay\":1,\n" +
                "   \"timestamp\":\"2020-03-20 12:12:12\",\n" +
                "   \"type\":2\n" +
                "  }\n" +
                " ],\n" +
                " \"incomeDatetime\":\"2020-03-20 12:12:12\",\n" +
                " \"ownParkCode\":\"testpark\",\n" +
                " \"receivable\":100,\n" +
                " \"unpaid\":50\n" +
                "},\n" +
                "\"exiting\":{\n" +
                " \"category\":\"临时车\",\n" +
                " \"discountFee\":11,\n" +
                " \"discountReason\":\"优惠原因\",\n" +
                " \"duration\":230,\n" +
                " \"exit\":\"车场出口\",\n" +
                " \"exitingWay\":2,\n" +
                " \"imgUrl\":\"http://demo.html\",\n" +
                " \"ownParkCode\":\"testpark\",\n" +
                " \"parkingDuration\":100,\n" +
                " \"parkingFee\":100,\n" +
                " \"timestamp\":\"2020-03-25 12:12:12\"\n" +
                "}\n" +
                " }";

        JSONObject o = (JSONObject) JSON.parse(s);
        o.forEach((key,value)->{
            System.out.println(key + ": " + value);
            if(value instanceof Map){
                ((Map) value).forEach((k,v) ->{
                    System.out.println("\t" + k + ": " + v);
                });
            }
        });
        o.put("noise", "1851497211");
        o.put("algorithm", "HmacSHA1");
        o.put("keyId", "a08cfd2c3d08");
        System.out.println(new String(new DefaultSignatureSerializer().serialize(o)));
    }
}
