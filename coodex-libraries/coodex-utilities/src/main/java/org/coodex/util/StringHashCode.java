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

/**
 *
 */
package org.coodex.util;

/**
 * @author davidoff
 */
public class StringHashCode {

    public static int BKDRHash(String str) {
        if (str == null)
            return 0;
        int seed = 131;
        int hash = 0;
        byte[] buf = str.getBytes();
        for (byte b : buf) {
            hash = hash * seed + (int) b;
        }
        return hash & 0x7FFFFFFF;
    }

}
