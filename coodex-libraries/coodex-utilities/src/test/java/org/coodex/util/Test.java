/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import java.lang.reflect.Constructor;

/**
 * Created by davidoff shen on 2016-10-19.
 */
public class Test {

    public static void main(String[] args) throws NoSuchMethodException {

        Class<?> x = X.class;

        Constructor<?> constructor = x.getConstructor(String.class, String.class);
        System.out.println(constructor.getParameterAnnotations().length);
        System.out.println(ReflectHelper.getParameterName(constructor, 0, "p"));
        System.out.println(ReflectHelper.getParameterName(constructor, 1, "p"));

//        System.out.println(JSON.toJSONString(new PojoInfo(B.class), true));
//        System.out.println(new PojoInfo(B.class).getProperty("x").getAnnotations()[0]);
//        System.out.println(new PojoInfo(B.class).getProperty("xxx2").getType());
//        System.out.println(new PojoInfo(new GenericType<A<List<String>>>(){}.genericType(B.class)).getProperty("xxx2").getType());

//        for(int i = 0; i < 10; i ++) {
////            System.out.println(MockerFacade.mock(int.class));
//            System.out.print(JSON.toJSONString(MockerFacade.mock(new GenericType<A<Byte>>() {
//            }), true));
////            System.out.println();
//        }
    }

    public static class X {
        public X(@Parameter("x1") String x1, String kkkk) {
        }

        public void abcd(@Parameter("ddd") String ddd, @Parameter("ok") String ok) {
        }
    }
}


