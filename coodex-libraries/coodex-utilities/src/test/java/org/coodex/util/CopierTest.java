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

package org.coodex.util;

import org.coodex.copier.AbstractCopier;
import org.coodex.copier.Copier;

public class CopierTest {
    public static class C1{}
    public static class C2{}

    private static Copier<C1,C2> copier = new AbstractCopier<C1, C2>() {
        @Override
        public C2 copy(C1 c1, C2 c2) {
            return c2;
        }
    };

    public static void main(String[] args) {
        System.out.println(copier.copy(new C1()));
    }
}
