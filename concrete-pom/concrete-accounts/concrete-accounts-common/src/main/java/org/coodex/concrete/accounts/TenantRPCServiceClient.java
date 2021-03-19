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

import java.io.Serializable;

/**
 * Created by davidoff shen on 2017-05-26.
 */
public interface TenantRPCServiceClient {

    void checkTenant(String tenantAccountName);

    TenantAccount getTenantAccountById(String id);

    TenantAccount getTenantAccount(String tenantAccountName);

    void login(String tenantAccountName, String password, String authCode);

    void updatePassword(String password, String authCode);

    String authenticatorDesc(String authCode);

    void bindAuthKey(String authCode);

    <T extends Serializable> void sendMessage(String msgName, T msgBody);
}
