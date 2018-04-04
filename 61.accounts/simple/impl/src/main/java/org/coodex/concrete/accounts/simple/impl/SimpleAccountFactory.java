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

package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.accounts.AccountIDImpl;
import org.coodex.concrete.common.AcceptableAccountFactory;
import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.AccountID;
import org.coodex.util.Profile;

import static org.coodex.concrete.accounts.AccountIDImpl.TYPE_SIMPLE;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccountFactory implements AcceptableAccountFactory<AccountIDImpl> {
    @Override
    public <ID extends AccountID> Account<ID> getAccountByID(ID id) {
        return (Account<ID>) new SimpleAccount((AccountIDImpl) id);
    }

    @Override
    public boolean accept(AccountIDImpl param) {
        boolean isSimple = param != null && param.getType() == TYPE_SIMPLE;

        return isSimple && Profile.getResource("accounts/" + param.getId() + ".properties") != null;
    }
}
