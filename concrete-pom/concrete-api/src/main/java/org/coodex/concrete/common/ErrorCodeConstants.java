/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.common;

public class ErrorCodeConstants {

    public static final int OK = 0;

    public static final int CUSTOM_LOWER_BOUND = 100000;

    public static final int CONCRETE_CORE = 1000;

    public static final int ATTACHMENT_ERROR_CODE = CONCRETE_CORE + 1000;

    public static final int REVERSE_PROXY_ERROR_CODE = CONCRETE_CORE + 2000;

    public static final int WEB_SOCKET_ERROR_CODE = CONCRETE_CORE + 3000;

    // 5000-5100 分配给prod


    // 10000 - 19999 保留给concrete accounts 模块
}
