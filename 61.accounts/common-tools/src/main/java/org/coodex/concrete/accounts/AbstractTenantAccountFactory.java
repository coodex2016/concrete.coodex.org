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

import static org.coodex.concrete.common.AccountsErrorCodes.NONE_THIS_ACCOUNT;

/**
 * Created by davidoff shen on 2017-05-26.
 */
public abstract class AbstractTenantAccountFactory implements AcceptableAccountFactory<AccountIDImpl> {

    private ConcreteCache<String, TenantAccount> accountCache = new ConcreteCache<String, TenantAccount>() {
        @Override
        protected TenantAccount load(String key) {
            return newAccount(key);
        }

        @Override
        protected String getRule() {
            return AbstractTenantAccountFactory.class.getPackage().getName();
        }
    };

    protected abstract TenantAccount newAccount(String key);

    @Override
    @SuppressWarnings({"unchecked", "unsafe"})
    public <ID extends AccountID> Account<ID> getAccountByID(ID id) {
        return (Account<ID>) IF.isNull(accountCache.get(((AccountIDImpl) id).getId()), NONE_THIS_ACCOUNT);
    }

    @Override
    public boolean accept(AccountIDImpl param) {
        return param != null && param.getType() == AccountIDImpl.TYPE_TENANT_ADMINISTRATOR;
    }
}
