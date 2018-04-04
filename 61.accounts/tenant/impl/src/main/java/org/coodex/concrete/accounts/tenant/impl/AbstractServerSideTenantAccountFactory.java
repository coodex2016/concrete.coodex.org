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

package org.coodex.concrete.accounts.tenant.impl;

import org.coodex.concrete.accounts.AccountIDImpl;
import org.coodex.concrete.accounts.AbstractTenantAccountFactory;
import org.coodex.concrete.accounts.TenantAccount;
import org.coodex.concrete.accounts.tenant.entities.AbstractTenantEntity;
import org.coodex.concrete.accounts.tenant.repositories.AbstractTenantRepo;
import org.coodex.concrete.common.*;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

import static org.coodex.concrete.accounts.AccountManagementRoles.TENANT_MANAGER;

/**
 * 租户管理端帐号工厂
 * Created by davidoff shen on 2017-05-26.
 */
public abstract class AbstractServerSideTenantAccountFactory<E extends AbstractTenantEntity> extends AbstractTenantAccountFactory {

    @Inject
    protected AbstractTenantRepo<E> tenantRepo;
    protected Copier<E, TenantAccount> copier = new AbstractCopier<E, TenantAccount>() {
        @Override
        public TenantAccount copy(E e, TenantAccount tenantAccount) {
            tenantAccount.setAppSet(e.getAppSet());
            tenantAccount.setName(e.getName());
            tenantAccount.setId(new AccountIDImpl(AccountIDImpl.TYPE_TENANT_ADMINISTRATOR, e.getId()));
            tenantAccount.setRoles(new HashSet<String>(Arrays.asList(TENANT_MANAGER)));
            tenantAccount.setTenant(tenantAccount.getName());
            tenantAccount.setValid(true);
            return tenantAccount;
        }
    };

    @Override
    protected TenantAccount newAccount(String key) {
        E person = tenantRepo.findOne(key);
        return person == null ? null : copier.copy(person);
    }


}
