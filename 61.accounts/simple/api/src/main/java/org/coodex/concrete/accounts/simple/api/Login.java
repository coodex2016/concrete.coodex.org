package org.coodex.concrete.accounts.simple.api;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.util.Parameter;

/**
 * Created by davidoff shen on 2017-07-05.
 */
@MicroService("Simple")
public interface Login extends ConcreteService {

    @Description(name = "帐号登录")
    String login(
            @Parameter("account")
            @BigString
                    String account,
            @Parameter("password")
            @Description(name = "密码")
            @BigString
                    String password,
            @Parameter("authCode")
            @BigString
                    String authCode);


    @Description(name = "注销登录", description = "注销后，缓存的令牌也同时失效")
    @AccessAllow
    void logout();
}
