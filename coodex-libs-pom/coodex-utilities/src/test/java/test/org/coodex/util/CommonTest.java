/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.util;

import org.coodex.util.Common;

public class CommonTest {

    public static void main(String[] args) {
        System.out.println(Common.FILE_SEPARATOR);
        System.out.println(Common.PATH_SEPARATOR);
        System.out.println(Common.DEFAULT_TIME_FORMAT);
        System.out.println(Common.sameString("1","2"));
    }
}
