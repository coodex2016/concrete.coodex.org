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

package org.coodex.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchableTest {
    @Test
    public void test() {
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        byte[] s1 = {4, 5, 6};
        byte[] s2 = {10, 11};
        byte[] s3 = {1, 10};
        ByteArraySearchable searchable = new ByteArraySearchable(bytes);
        Assertions.assertEquals(searchable.indexOf(0, 10, s1), 3);
        Assertions.assertEquals(searchable.indexOf(1, 9, s1), 3);
        Assertions.assertEquals(searchable.indexOf(2, 8, s1), 3);
        Assertions.assertEquals(searchable.indexOf(3, 7, s1), 3);
        Assertions.assertEquals(searchable.indexOf(3, 6, s1), 3);
        Assertions.assertEquals(searchable.indexOf(3, 5, s1), -1);

        Assertions.assertEquals(searchable.indexOf(1, 10, s2), -1);
        Assertions.assertEquals(searchable.indexOf(1, 10, s3), -1);
    }

    @Test
    public void test2() {
        int[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] s1 = {4, 5, 6};
        int[] s2 = {10, 11};
        int[] s3 = {1, 10};
        IntArraySearchable searchable = new IntArraySearchable(bytes);
        Assertions.assertEquals(searchable.indexOf(0, 10, s1), 3);
        Assertions.assertEquals(searchable.indexOf(1, 9, s1), 3);
        Assertions.assertEquals(searchable.indexOf(2, 8, s1), 3);
        Assertions.assertEquals(searchable.indexOf(3, 7, s1), 3);
        Assertions.assertEquals(searchable.indexOf(3, 6, s1), 3);
        Assertions.assertEquals(searchable.indexOf(3, 5, s1), -1);

        Assertions.assertEquals(searchable.indexOf(1, 10, s2), -1);
        Assertions.assertEquals(searchable.indexOf(1, 10, s3), -1);
    }

    static class ByteArraySearchable implements Searchable.Bytes {
        private final byte[] bytes;

        ByteArraySearchable(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int indexOf(int fromIndex, int toIndex, Byte element) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (element == bytes[i]) return i;
            }
            return -1;
        }

        @Override
        public Byte get(int index) {
            return bytes[index];
        }
    }

    static class IntArraySearchable implements Searchable.Integers {
        private final int[] bytes;

        IntArraySearchable(int[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int indexOf(int fromIndex, int toIndex, Integer element) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (element == bytes[i]) return i;
            }
            return -1;
        }

        @Override
        public Integer get(int index) {
            return bytes[index];
        }
    }
}
