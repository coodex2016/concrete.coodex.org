/*
 * Copyright (c) 2016 - 2025 coodex.org (jujus.shen@126.com)
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

//import static jdk.nashorn.internal.objects.NativeString.indexOf;

import java.util.Objects;

public class SearchableHelper {
    public static <E> int indexOf(Searchable<E> searchable, int fromIndex, int toIndex, E[] elements) {
        if (elements == null || elements.length == 0) return fromIndex;
        if (elements.length == 1) {
            return searchable.indexOf(fromIndex, toIndex, elements[0]);
        }

        int len = elements.length;
        if (fromIndex + len > toIndex) return -1;
        while (fromIndex + len <= toIndex) {
            int first = searchable.indexOf(fromIndex, toIndex - len + 1, elements[0]);
            if (first >= 0) {
                boolean ok = true;
                for (int i = 1; i < len; i++) {
                    if (!Objects.equals(elements[i], searchable.get(first + i))) {
                        ok = false;
                        break;
                    }
                }
                if (ok) return first;
                fromIndex = first + 1;
            } else {
                break;
            }
        }
        return -1;
    }

    public static int indexOf_int(Searchable<Integer> searchable, int fromIndex, int toIndex, int[] elements) {
        if (elements == null || elements.length == 0) return fromIndex;
        Integer[] integers = new Integer[elements.length];
        for (int i = 0, l = integers.length; i < l; i++) {
            integers[i] = elements[i];
        }
        return indexOf(searchable, fromIndex, toIndex, integers);
    }

    public static int indexOf_byte(Searchable<Byte> searchable, int fromIndex, int toIndex, byte[] elements) {
        if (elements == null || elements.length == 0) return fromIndex;
        Byte[] bytes = new Byte[elements.length];
        for (int i = 0, l = bytes.length; i < l; i++) {
            bytes[i] = elements[i];
        }
        return indexOf(searchable, fromIndex, toIndex, bytes);
    }
}
