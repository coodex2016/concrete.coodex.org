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

import org.coodex.concrete.Client;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;

import java.io.Serializable;

import static org.coodex.concrete.accounts.AccountsCommon.getTenant;

/**
 * 基于concrete-jaxrs-client的租户验证客户端
 * <p>
 * 不推荐所有业务都进行租户检查。如果需要，可以考虑缓存方案
 * <p>
 * Created by davidoff shen on 2017-05-26.
 */
public class TenantRPCServiceClientImpl implements TenantRPCServiceClient {

    protected Token token = TokenWrapper.getInstance();


    protected TenantRPCService getRPCService() {
        return Client.getInstance(TenantRPCService.class,
                ConcreteHelper.getProfile().getString("tenant.RPC.service"));
    }

    @Override
    public void checkTenant(String tenantAccountName) {
        getRPCService().checkTenant(tenantAccountName);
    }

    @Override
    public TenantAccount getTenantAccountById(String id) {
        return getRPCService().getTenantAccountById(id);
    }

    @Override
    public TenantAccount getTenantAccount(String tenantAccountName) {
        return getRPCService().getTenantAccount(tenantAccountName);
    }


    @Override
    public void login(String tenantAccountName, String password, String authCode) {
        boolean credible = getRPCService().login(tenantAccountName, password, authCode);
        token.setAccount(getTenantAccount(tenantAccountName));
        token.setAccountCredible(credible);
    }

    @Override
    public void updatePassword(String password, String authCode) {
        getRPCService().updatePassword(getTenant(), password, authCode);
    }

    @Override
    public String authenticatorDesc(String authCode) {
        return getRPCService().authenticatorDesc(getTenant(), authCode);
    }

    @Override
    public void bindAuthKey(String authCode) {
        getRPCService().bindAuthKey(getTenant(), authCode);
    }

    @Override
    public <T extends Serializable> void sendMessage(String msgName, T msgBody) {
        getRPCService().sendMessage(getTenant(), msgName, JSONSerializerFactory.getInstance().toJson(msgBody));
    }
}
