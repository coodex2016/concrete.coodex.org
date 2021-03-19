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

package org.coodex.concrete.accounts;

import org.coodex.concrete.common.AccountID;
import org.coodex.concrete.common.AccountIDDeserializer;

/**
 * Created by davidoff shen on 2017-07-17.
 * @deprecated
 */
@Deprecated
public class AccountIDImplDeserializer implements AccountIDDeserializer {

    private static final String START_WITH = "CONCRETE-ACCOUNTS:";
    private static final String SPLIT_BY = ",";


    final static String serialize(AccountIDImpl accountID) {
        return String.format("%s%d%s%s", START_WITH, accountID.getType(), SPLIT_BY, accountID.getId());
    }


    @Override
    public AccountID deserialize(String accountIDStr) {
        int type = 0;
        String id = null;
        accountIDStr = accountIDStr.substring(START_WITH.length());
        int i = accountIDStr.indexOf(SPLIT_BY);
        type = Integer.valueOf(accountIDStr.substring(0, i));
        id = accountIDStr.substring(i + SPLIT_BY.length());
        return new AccountIDImpl(type, id);
    }

    @Override
    public boolean accept(String accountIDStr) {
        return accountIDStr != null && accountIDStr.startsWith(START_WITH);
    }
}
