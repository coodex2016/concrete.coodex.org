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
 * Created by davidoff shen on 2017-04-28.
 */
public class AccountsErrorCodes extends AbstractErrorCodes {

    protected static final int ACCOUNT_BASE = 10000;
    public static final int NONE_THIS_ACCOUNT = ACCOUNT_BASE + 1;
    public static final int LOGIN_FAILED = ACCOUNT_BASE + 2;
    public static final int NONE_THIS_CREDENTIAL = ACCOUNT_BASE + 3;
    public static final int AUTHORIZE_FAILED = ACCOUNT_BASE + 4;
    public static final int ACCOUNT_NOT_ACTIVED = ACCOUNT_BASE + 5;
    public static final int AUTH_KEY_FAILURE = ACCOUNT_BASE + 6;
    public static final int TENANT_NOT_EXISTS = ACCOUNT_BASE + 7;
    public static final int TENANT_UNAVAILABLE = ACCOUNT_BASE + 8;
    public static final int TENANT_ALREADY_EXISTS = ACCOUNT_BASE + 9;
    public static final int NONE_TENANT_ARCHITECTURE = ACCOUNT_BASE + 10;
    public static final int TENANT_IN_USING = ACCOUNT_BASE + 11;
    public static final int TENANT_CANNOT_DELETE = ACCOUNT_BASE + 12;
    protected static final int ORGANIZATION_BASE = ACCOUNT_BASE + 1000;
}
