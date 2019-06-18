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

package org.coodex.concrete.accounts.simple.api;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;
import org.coodex.util.Parameter;

/**
 * Created by davidoff shen on 2017-07-05.
 */
@MicroService("Simple")
public interface Login {

    @Description(name = "帐号登录")
    String login(
            @Parameter("account")
                    String account,
            @Parameter("password")
            @Description(name = "密码")
                    String password,
            @Parameter("authCode")
                    String authCode);


    @Description(name = "注销登录", description = "注销后，缓存的令牌也同时失效")
    @AccessAllow
    void logout();
}
