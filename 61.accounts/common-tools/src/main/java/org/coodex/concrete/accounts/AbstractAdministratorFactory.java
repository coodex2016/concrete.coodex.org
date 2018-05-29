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

import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenWrapper;

import static org.coodex.concrete.accounts.AccountIDImpl.TYPE_ADMINISTRATOR;
import static org.coodex.concrete.common.AccountsErrorCodes.LOGIN_FAILED;

/**
 * Created by davidoff shen on 2017-05-19.
 */
public abstract class AbstractAdministratorFactory implements AcceptableAccountFactory<AccountIDImpl> {

    private Token token = TokenWrapper.getInstance();

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends AccountID> Account<ID> getAccountByID(ID id) {
        if (id == null || !(id instanceof AccountIDImpl)) return null;
        return (Account<ID>) getAdministrator(((AccountIDImpl) id).getId());
    }

    @Override
    public boolean accept(AccountIDImpl param) {
        return param != null && param.getType() == TYPE_ADMINISTRATOR;
    }


//    public void login(String id, String password, String authCode) {
//        Administrator administrator = getAdministrator(id);
//        if (administrator.verify(password, authCode)) {
//            token.setAccount(administrator);
//            token.setAccountCredible(true);
//        } else {
//            throw new ConcreteException(LOGIN_FAILED);
//        }
//    }


    public void login(String tenant, String id, String password, String authCode) {
        Administrator administrator = getAdministrator(id, tenant);
        if (administrator.verify(password, authCode)) {
            token.setAccount(administrator);
            token.setAccountCredible(true);
        } else {
            throw new ConcreteException(LOGIN_FAILED);
        }
    }


    protected abstract Administrator getAdministrator(String id);

    protected abstract Administrator getAdministrator(String id, String tenant);
}
