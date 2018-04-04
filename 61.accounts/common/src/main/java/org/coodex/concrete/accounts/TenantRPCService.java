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

import org.coodex.concrete.api.Abstract;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Signable;

/**
 * 检查租户的RPC服务接口，由租户管理端实现并提供服务，请求必须签名。其他云账户平台根据架构选择合适的客户端
 * <p>
 * Created by davidoff shen on 2017-05-26.
 */
@Abstract
@Signable(paperName = "tenantRPCService")
public interface TenantRPCService extends ConcreteService {

    /**
     * 如果租户不存在，抛出AccountsErrorCodes.TENANT_NOT_EXISTS;
     * <p>
     * 如果租户有效期超期，或租户停用中，抛出AccountsErrorCodes.TENANT_UNAVAILABLE
     *
     * @param tenantAccountName
     */
    void checkTenant(String tenantAccountName);

    TenantAccount getTenantAccountById(String id);

    TenantAccount getTenantAccount(String tenantAccountName);

    /**
     * 可信:true
     *
     * @param tenant
     * @param password
     * @param authCode
     * @return
     */
    boolean login(String tenant, String password, String authCode);

    void updatePassword(String tenant, String password, String authCode);

    String authenticatorDesc(String tenant, String authCode);

    void bindAuthKey(String tenant, String authCode);

    void sendMessage(String tenant, String msgName, String msgBody);

}
