/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

import java.nio.ByteBuffer;

public class Base58Test {
    public static void main(String[] args) {
        test(Long.MAX_VALUE);
        test(0L);
        test(Long.MIN_VALUE);
        test(0xFFFFFFFFFFFFFFFFL);
    }

    private static void test(long l){
        String base58 = Base58.encode(ByteBuffer.allocate(8).putLong(l).array());
        System.out.printf("%d: %s, length: %d\n",l, base58, base58.length());
    }
}
