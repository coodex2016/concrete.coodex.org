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

package test.org.coodex.test;

import org.coodex.util.Common;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLibTest {
    private static A getProxyInstance(A a) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(A.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                if (method.getDeclaringClass().equals(A.class)) {
                    System.out.println(method);
                }
                return method.invoke(a, objects);
            }
        });
        return Common.cast(enhancer.create());
    }

    public static void main(String[] args) {
        A a = new A();
        System.out.println(getProxyInstance(a).getClass());
        System.out.println(getProxyInstance(a).getClass());
        System.out.println(getProxyInstance(a).getClass());

        getProxyInstance(a).aaa();

    }

    public static class A {

        public void aaa(){}
    }
}


