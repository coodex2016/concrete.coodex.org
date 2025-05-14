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
import org.coodex.concrete.api.ErrorCodeDef;

import static org.coodex.concrete.common.AccountsErrorCodes.ORGANIZATION_BASE;

/**
 * Created by davidoff shen on 2017-04-28.
 */
@ErrorCode("message.concrete.account")
public class OrganizationErrorCodes implements ErrorCodeDef/*extends AccountsErrorCodes */ {


    protected static final int BASE = ORGANIZATION_BASE;
    @ErrorCode.Key("noneInstitution")
    @ErrorCode.RequestError
    public final static int NONE_THIS_INSTITUTION = BASE + 1;

    @ErrorCode.Key("noneDepartment")
    @ErrorCode.RequestError
    public final static int NONE_THIS_DEPARTMENT = BASE + 2;
    @ErrorCode.Key("noneOrganization")
    @ErrorCode.RequestError
    public static final int NONE_THIS_ORGANIZATION = BASE + 3;

    @ErrorCode.Key("duplicatedName")
    @ErrorCode.RequestError
    public static final int DUPLICATED_NAME = BASE + 4;

    @ErrorCode.Key("levelCirculation")
    @ErrorCode.RequestError
    public static final int HIGHER_LEVEL_CIRCULATION = BASE + 5;

    @ErrorCode.Key("position.cannotDelete")
    public static final int POSITION_CANNOT_DELETE = BASE + 6;

    @ErrorCode.Key("position.notExists")
    @ErrorCode.RequestError
    public static final int POSITION_NOT_EXISTS = BASE + 7;

    @ErrorCode.Key("person.notExists")
    @ErrorCode.RequestError
    public static final int PERSON_NOT_EXISTS = BASE + 8;

    @ErrorCode.Key("notOrganizationAccount")
    @ErrorCode.RequestError
    public static final int NOT_ORGANIZATION_ACCOUNT = BASE + 9;

    @ErrorCode.Key("cellPhoneExists")
    @ErrorCode.RequestError
    public static final int CELL_PHONE_EXISTS = BASE + 10;

    @ErrorCode.Key("idCardNoExists")
    @ErrorCode.RequestError
    public static final int ID_CARD_NO_EXISTS = BASE + 11;

    @ErrorCode.Key("emailExists")
    @ErrorCode.RequestError
    public static final int EMAIL_EXISTS = BASE + 12;

}
