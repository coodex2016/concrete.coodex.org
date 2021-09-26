/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

public class Proxy {

    interface A {
        void method1();

        default void method2() {
            method1();
        }
    }

    static class AImpl implements A {

        @Override
        public void method1() {
            System.out.println("AImpl method1");
        }
    }


    // 类似动态代理机制
    static class ADynamicProxy implements A {
        private final A instance;

        ADynamicProxy(A instance) {
            this.instance = instance;
        }

        @Override
        public void method1() {
            System.out.println("method1 AOP by dynamic proxy");
            instance.method1();
        }

        @Override
        public void method2() {
            System.out.println("method2 AOP by dynamic proxy");
            instance.method2();
        }
    }

    // 类似动态继承机制
    static class ADynamicExtends extends AImpl {
        @Override
        public void method2() {
            System.out.println("method2 AOP by dynamic extends");
            super.method2();
        }

        @Override
        public void method1() {
            System.out.println("method1 AOP by dynamic extends");
            super.method1();
        }
    }

    public static void main(String[] args) {
        A a = new AImpl();// bean
        A dp = new ADynamicProxy(a);// 类似动态代理模式
        A de = new ADynamicExtends();// 类似动态继承模式
        System.out.println("--------");
        dp.method2();
        System.out.println("--------");
        de.method2();
    }
}
