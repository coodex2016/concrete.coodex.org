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

package org.coodex.concrete.common;


/**
 * concrete 系统用到的错误号定义
 * Created by davidoff shen on 2016-09-01.
 */
public final class ErrorCodes extends AbstractErrorCodes {

    public static final int UNKNOWN_ERROR = CUSTOM_LOWER_BOUND - 1;

    public static final int CLIENT_ERROR = UNKNOWN_ERROR - 1;


    public static final int MODULE_DEFINITION_NOT_FOUND = CONCRETE_CORE + 1;

    public static final int UNIT_DEFINITION_NOT_FOUND = CONCRETE_CORE + 2;

    public static final int NONE_TOKEN = CONCRETE_CORE + 3;

    public static final int TOKEN_INVALIDATE = CONCRETE_CORE + 4;

    public static final int NONE_ACCOUNT = CONCRETE_CORE + 5;

    public static final int NO_AUTHORIZATION = CONCRETE_CORE + 6;

    public static final int ACCOUNT_INVALIDATE = CONCRETE_CORE + 7;

    public static final int UNTRUSTED_ACCOUNT = CONCRETE_CORE + 8;

    public static final int DATA_VIOLATION = CONCRETE_CORE + 9;

    public static final int NO_BEAN_PROVIDER_FOUND = CONCRETE_CORE + 10;

    public static final int NO_SERVICE_INSTANCE_FOUND = CONCRETE_CORE + 11;

    public static final int BEAN_CONFLICT = CONCRETE_CORE + 12;

    public static final int OUT_OF_SERVICE_TIME = CONCRETE_CORE + 13;

    public static final int OVERRUN = CONCRETE_CORE + 14;

    public static final int SIGNING_FAILED = CONCRETE_CORE + 15;

    public static final int SIGNATURE_VERIFICATION_FAILED = CONCRETE_CORE + 16;

    public static final int UNKNOWN_CLASS = CONCRETE_CORE + 17;

    public static final int MODULE_DEFINITION_NON_UNIQUENESS = CONCRETE_CORE + 18;
}
