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

import org.coodex.concrete.common.AccountsErrorCodes;
import org.coodex.concrete.common.ConcreteException;

import java.io.Serializable;

/**
 * 非租户体系的租户验证服务客户端
 * Created by davidoff shen on 2017-05-26.
 */
public class NoneTenantArchitecture implements TenantRPCServiceClient {

    private ConcreteException noneTenantArchitecture = new ConcreteException(AccountsErrorCodes.NONE_TENANT_ARCHITECTURE);

    @Override
    public void checkTenant(String tenantAccountName) {
        // do nothing
    }

    @Override
    public TenantAccount getTenantAccountById(String id) {
        throw noneTenantArchitecture;
    }

    @Override
    public TenantAccount getTenantAccount(String tenantAccountName) {
        throw noneTenantArchitecture;
    }

    @Override
    public void login(String tenantAccountName, String password, String authCode) {
        throw noneTenantArchitecture;
    }

    @Override
    public void updatePassword(String password, String authCode) {
        throw noneTenantArchitecture;
    }

    @Override
    public String authenticatorDesc(String authCode) {
        throw noneTenantArchitecture;
    }

    @Override
    public void bindAuthKey(String authCode) {
        throw noneTenantArchitecture;
    }

    @Override
    public <T extends Serializable>void sendMessage(String msgName, T msgBody) {
        // do nothing
    }
}
