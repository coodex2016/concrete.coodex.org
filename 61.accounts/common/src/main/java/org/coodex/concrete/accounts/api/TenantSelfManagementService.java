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

package org.coodex.concrete.accounts.api;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.Safely;
import org.coodex.util.Parameter;

/**
 * 租户自管理服务，部署在应用端
 * Created by davidoff shen on 2017-05-26.
 */
@MicroService("tenant")
public interface TenantSelfManagementService {

    @MicroService("{tenantAccountName}/login")
    void login(
            @Parameter("tenantAccountName") String tenantAccountName,
            @Parameter("password") String password,
            @Parameter("authCode") String authCode);

    @AccessAllow
    @Safely
    @MicroService("mine/pwd")
    void updatePassword(
            @Parameter("password") String password,
            @Parameter("authCode") String authCode);

    @AccessAllow
    @MicroService("mine/totp")
    String authenticatorDesc(
            @Parameter("authCode") String authCode);

    @AccessAllow
    @MicroService("mine/auth")
    void bindAuthKey(@Parameter("authCode") String authCode);
}
