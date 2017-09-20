/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.test.pojo;

import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.util.GenericType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestCase {

    public static void main(String [] args){
        JSONSerializer serializer = JSONSerializerFactory.getInstance();
        C c = new C();
        String json_c = serializer.toJson(c);
        G<List<C>> gc = new G<List<C>>();
        gc.setContent(Arrays.asList(c));
        String json_gc = serializer.toJson(gc);

        G<Object> gs = serializer.parse(json_gc, new GenericType<G<Object>>(){}.genericType());

        String parse_c = serializer.toJson(gs.getContent());

        List<C> cList = serializer.parse(parse_c, new GenericType<List<C>>(){}.genericType());

        System.out.println(json_c);
        System.out.println(json_gc);
        System.out.println(parse_c);



    }
}
