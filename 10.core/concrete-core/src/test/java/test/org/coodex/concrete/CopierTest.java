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

package test.org.coodex.concrete;

import org.coodex.concrete.common.AbstractCopier;
import org.coodex.concrete.common.Copier;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.util.GenericType;

import java.util.List;

/**
 * Created by davidoff shen on 2017-03-21.
 */
public class CopierTest {
    public static void main(String[] args) {
        Copier<A, B> copier = new AbstractCopier<A, B>() {
            @Override
            public B copy(A a, B b) {
                b.valueOfInt = String.valueOf(a.anInt);
                return b;
            }
        };
        copier.copy(new A(10)).test();

        System.out.println(JavassistHelper.classType(new GenericType<List<String>>(){}.genericType(), null));
    }

    public static class A {
        int anInt;

        public A(int anInt) {
            this.anInt = anInt;
        }
    }

    public static class B {
        String valueOfInt;

        void test() {
            System.out.println(valueOfInt);
        }
    }
}
