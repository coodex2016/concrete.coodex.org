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

package org.coodex.concrete.accounts.organization.api;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;
import org.coodex.util.Parameter;

/**
 * Created by davidoff shen on 2017-05-03.
 */
@MicroService(abstractive = true)
public interface AbstractLoginService {

    @Description(name = "帐号登录", description = "返回值为缓存信息，用于有效期内免秘登录")
    String login(
            @Parameter("tenant")
                    String tenant,
            @Description(name = "帐号", description = "可以是身份证号/邮箱/手机号")
            @Parameter("account")
                    String account,
            @Description(name = "密码")
            @Parameter("password")
                    String password,
            @Description(name = "认证码",
                    description = "为空则表示可能尚未绑定认证码或认证码尚未生效")
            @Parameter("authCode")
                    String authCode);

    @MicroService("login/administrator")
    @Description(name = "系统管理员登录", description = "用于系统初始化管理")
    void administratorLogin(
            @Parameter("tenant") String tenant,
            @Parameter("password") String password,
            @Parameter("authCode") String authCode);

    @MicroService("login/credential")
    @Description(name = "使用缓存的令牌登录", description = "登录后账户为不可信状态")
    void loginWith(
            @Parameter("credential")
                    String credential);


    @Description(name = "使用授权码验证身份", description = "验证成功后，当前令牌账户置为可信状态")
    @MicroService("login/identification")
    @AccessAllow
    String identification(
            @Parameter("authCode")
                    String authCode);


    @Description(name = "注销登录", description = "注销后，缓存的令牌也同时失效")
    @AccessAllow
    void logout();

}
