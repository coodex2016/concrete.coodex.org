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


import org.coodex.concrete.api.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.coodex.concrete.common.ErrorCodeConstants.CONCRETE_CORE;
import static org.coodex.concrete.common.ErrorCodeConstants.CUSTOM_LOWER_BOUND;

/**
 * concrete 系统用到的错误号定义
 * Created by davidoff shen on 2016-09-01.
 */
@ErrorCode
public final class ErrorCodes /*extends AbstractErrorCodes*/ {

    public static final int UNKNOWN_ERROR = CUSTOM_LOWER_BOUND - 1;
    public static final int CLIENT_ERROR = UNKNOWN_ERROR - 1;
    public static final int MODULE_DEFINITION_NOT_FOUND = CONCRETE_CORE + 1;
    public static final int UNIT_DEFINITION_NOT_FOUND = CONCRETE_CORE + 2;
    @RequestError
    public static final int NONE_TOKEN = CONCRETE_CORE + 3;
    @RequestError
    public static final int TOKEN_INVALIDATE = CONCRETE_CORE + 4;
    @RequestError
    public static final int NONE_ACCOUNT = CONCRETE_CORE + 5;
    @RequestError
    public static final int NO_AUTHORIZATION = CONCRETE_CORE + 6;
    @RequestError
    public static final int ACCOUNT_INVALIDATE = CONCRETE_CORE + 7;
    @RequestError
    public static final int UNTRUSTED_ACCOUNT = CONCRETE_CORE + 8;
    @RequestError
    public static final int DATA_VIOLATION = CONCRETE_CORE + 9;
    public static final int NO_BEAN_PROVIDER_FOUND = CONCRETE_CORE + 10;
    public static final int NO_SERVICE_INSTANCE_FOUND = CONCRETE_CORE + 11;
    public static final int BEAN_CONFLICT = CONCRETE_CORE + 12;
    @RequestError
    public static final int OUT_OF_SERVICE_TIME = CONCRETE_CORE + 13;
    @RequestError
    public static final int OVERRUN = CONCRETE_CORE + 14;
    public static final int SIGNING_FAILED = CONCRETE_CORE + 15;
    @RequestError
    public static final int SIGNATURE_VERIFICATION_FAILED = CONCRETE_CORE + 16;
    public static final int UNKNOWN_CLASS = CONCRETE_CORE + 17;
    public static final int MODULE_DEFINITION_NON_UNIQUENESS = CONCRETE_CORE + 18;
    public static final int SIGNATURE_KEY_LOAD_FAILED = CONCRETE_CORE + 19;
    public static final int NONE_IMPLEMENTS_FOUND_FOR = CONCRETE_CORE + 20;
    //
    @RequestError
    public static final int ABOUT_LICENSE = CONCRETE_CORE + 100;
    @RequestError
    public static final int PRODUCTION_OVERDUE = CONCRETE_CORE + 101;
    @RequestError
    public static final int PRODUCTION_OVERDUE_REMIND = CONCRETE_CORE + 102;
    @RequestError
    public static final int PRODUCTION_NONE_THIS_MODULE = CONCRETE_CORE + 103;
    ///Own protocol
    public static final int OWN_PROVIDER_NO_RESPONSE_VISITOR = CONCRETE_CORE + 200;
    @RequestError
    public static final int SERVICE_ID_NOT_EXISTS = CONCRETE_CORE + 201;
    public static final int WARNING_DEPRECATED = CONCRETE_CORE + 300;

    /**
     * 该类型为客户端错误，服务端默认不显示
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RequestError {
        boolean trace() default false;
    }

}
