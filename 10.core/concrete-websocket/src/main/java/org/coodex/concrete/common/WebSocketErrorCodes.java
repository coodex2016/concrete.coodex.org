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

package org.coodex.concrete.common;

public class WebSocketErrorCodes extends AbstractErrorCodes {

    protected final static int LOWER_BOUND = WEB_SOCKET_ERROR_CODE;


    // 带一个参数，serviceId
    public static final int SERVICE_ID_NOT_EXISTS = LOWER_BOUND + 1;
    // 带一个参数，domain
    public static final int CANNOT_OPEN_SESSION = LOWER_BOUND + 2;
    // 带一个参数，unitBaseKey
    public static final int UNIT_NOT_EXISTS = LOWER_BOUND + 3;

    public static final int RESPONSE_TIMEOUT = LOWER_BOUND + 4;
}
