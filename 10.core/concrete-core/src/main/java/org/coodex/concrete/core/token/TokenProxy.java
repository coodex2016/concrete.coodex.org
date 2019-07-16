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

package org.coodex.concrete.core.token;

import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.AccountID;
import org.coodex.concrete.common.Token;

import java.io.Serializable;
import java.util.Enumeration;

import static org.coodex.concrete.core.token.TokenWrapper.getToken;
import static org.coodex.concrete.core.token.TokenWrapper.newToken;

/**
 * Token的代理，当被代理的token为空或者无效时，进行set操作会创建token；
 * token为空时，getTokenId也为空
 */
public class TokenProxy implements Token {

//    private static Singleton<TokenManager> tokenManager =
//            new Singleton<TokenManager>(new Singleton.Builder<TokenManager>() {
//                @Override
//                public TokenManager build() {
//                    return BeanServiceLoaderProvider.getBeanProvider().getBean(TokenManager.class);
//                }
//            });

    private Token proxy;

    private TokenProxy(Token proxy) {
        this.proxy = proxy;
    }

    public static Token proxy(String tokenId) {
        return new TokenProxy(tokenId == null ? null :
                getToken(tokenId));
    }

    @Override
    public long created() {
        return this.proxy == null ? 0 : this.proxy.created();
    }

    @Override
    public boolean isValid() {
        return proxy == null || proxy.isValid();
    }

    @Override
    public void invalidate() {
        if (this.proxy != null)
            this.proxy.invalidate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends AccountID> Account<ID> currentAccount() {
        return this.proxy == null ? null : this.proxy.<ID>currentAccount();
    }

    private Token forSet() {
        if (proxy == null || !proxy.isValid()) {
            synchronized (this) {
                if (proxy == null || !proxy.isValid())
                    proxy = newToken();
            }
        }
        return proxy;
    }

    @Override
    public void setAccount(Account account) {
        forSet().setAccount(account);
    }

    @Override
    public boolean isAccountCredible() {
        return proxy == null ? false : proxy.isAccountCredible();
    }

    @Override
    public void setAccountCredible(boolean credible) {
        forSet().setAccountCredible(credible);
    }

    @Override
    public String getTokenId() {
        return proxy == null ? null : proxy.getTokenId();
    }

    @Override
    @Deprecated
    public <T> T getAttribute(String key) {
        return proxy == null ? null : (T) proxy.getAttribute(key);
    }

    @Override
    public <T> T getAttribute(String key, Class<T> clz) {
        return proxy == null ? null : proxy.getAttribute(key, clz);
    }

    @Override
    public void setAttribute(String key, Serializable attribute) {
        forSet().setAttribute(key, attribute);
    }

    @Override
    public void removeAttribute(String key) {
        if (proxy != null)
            proxy.removeAttribute(key);
    }

    @Override
    public Enumeration<String> attributeNames() {
        return proxy == null ? null : proxy.attributeNames();
    }

    @Override
    public void flush() {
        if (proxy != null)
            proxy.flush();
    }

    @Override
    public void renew() {
        if (proxy != null)
            proxy.renew();
    }
}
