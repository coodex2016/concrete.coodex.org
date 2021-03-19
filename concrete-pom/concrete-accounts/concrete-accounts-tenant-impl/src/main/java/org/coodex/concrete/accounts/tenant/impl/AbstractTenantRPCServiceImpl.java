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

import org.coodex.concrete.accounts.AccountsCommon;
import org.coodex.concrete.accounts.TenantAccount;
import org.coodex.concrete.accounts.TenantRPCService;
import org.coodex.concrete.accounts.tenant.entities.AbstractTenantEntity;
import org.coodex.concrete.accounts.tenant.repositories.AbstractTenantRepo;
import org.coodex.concrete.common.IF;
import org.coodex.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Calendar;

import static org.coodex.concrete.accounts.AccountsCommon.*;
import static org.coodex.concrete.common.AccountsErrorCodes.TENANT_NOT_EXISTS;
import static org.coodex.concrete.common.AccountsErrorCodes.TENANT_UNAVAILABLE;

/**
 * Created by davidoff shen on 2017-05-26.
 */
public abstract class AbstractTenantRPCServiceImpl<E extends AbstractTenantEntity> implements TenantRPCService {

    private final static Logger log = LoggerFactory.getLogger(AbstractTenantRPCServiceImpl.class);

    @Inject
    protected AbstractTenantRepo<E> repo;

    @Inject
    protected AbstractServerSideTenantAccountFactory<E> accountFactory;

    protected E getTenantEntity(String tenantAccountName) {
        return IF.isNull(repo.findFirstByAccountName(tenantAccountName), TENANT_NOT_EXISTS);
    }

    @Override
    public void checkTenant(String tenantAccountName) {
        E tenantEntity = getTenantEntity(tenantAccountName);
        Calendar calendar = tenantEntity.getValidation();
        IF.not(
                tenantEntity.isUsing() && calendar != null &&
                        calendar.getTimeInMillis() >= Clock.currentTimeMillis()
                , TENANT_UNAVAILABLE);
    }

    @Override
    public TenantAccount getTenantAccount(String tenantAccountName) {
        return accountFactory.newAccount(getTenantEntity(tenantAccountName).getId());
    }

    @Override
    public TenantAccount getTenantAccountById(String id) {
        return accountFactory.newAccount(IF.isNull(repo.findById(id).orElse(null), TENANT_NOT_EXISTS).getId());
    }

    @Override
    public boolean login(String tenant, String password, String authCode) {
        E tenantEntity = getTenantEntity(tenant);
        checkPassword(password, tenantEntity);
        return isCredible(authCode, tenantEntity);
    }

    @Override
    public void updatePassword(String tenant, String password, String authCode) {
        AccountsCommon.updatePassword(getTenantEntity(tenant), password, authCode, repo);
    }

    @Override
    public String authenticatorDesc(String tenant, String authCode) {
        return getAuthenticatorDesc(getTenantEntity(tenant), authCode);
    }

    @Override
    public void bindAuthKey(String tenant, String authCode) {
        AccountsCommon.bindAuthKey(getTenantEntity(tenant), authCode, repo);
    }

    @Override
    public void sendMessage(String tenant, String msgName, String msgBody) {
        // TODO, 建立消息处理机制
        log.info("received message: [tenant = {}, msgName = {}, msgBody = {}]", tenant, msgName, msgBody);
    }
}
