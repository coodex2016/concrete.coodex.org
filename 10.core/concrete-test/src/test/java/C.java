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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public class C extends A {
//
//    @Rule
//    public final B b = new B();

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        D d = new D();
        for (Method method : I2.class.getMethods()) {
            System.out.println(String.format("%s, %d, %s", method.getName(), method.getModifiers(), method.getDeclaringClass().getName()));
            method.invoke(d);
        }
    }
}
