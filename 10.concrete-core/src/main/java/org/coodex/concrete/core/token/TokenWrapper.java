/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.Token;

import java.io.Serializable;
import java.util.Enumeration;

import static org.coodex.concrete.common.ConcreteContext.TOKEN;

/**
 * 基于当前线程上下文提供<s>Session</s> Token
 * <i><s>TO</s><s>DO: 考虑到需要支持servlet 3.0和其他需要异步的i/o提供者，需要支持异步获取会话</s></i>
 * 上述内容无需由TokenWrapper考虑，应有I/O服务提供者层提供
 * Created by davidoff shen on 2016-09-05.
 */
public class TokenWrapper implements Token {


    private static final Token singletonInstance = new TokenWrapper();

    private Token getToken() {
        return getToken(true);
    }

    private Token getToken(boolean checkValidation) {
        Token token = TOKEN.get();
        Assert.isNull(token, ErrorCodes.NONE_TOKEN);
        Assert.is(checkValidation && !token.isValid(), ErrorCodes.TOKEN_INVALIDATE, token.getTokenId());
        return token;
    }

    public static final Token getInstance() {
        return singletonInstance;
    }

    @Override
    public long created() {
        return getToken().created();
    }

    @Override
    public boolean isValid() {
        return getToken(false).isValid();
    }

    @Override
    public void invalidate() {
        getToken().invalidate();
    }

    @Override
    public void onInvalidate() {
        getToken().onInvalidate();
    }

    @Override
    public <ID extends Serializable> Account<ID> currentAccount() {
        return getToken().currentAccount();
    }

    @Override
    public void setAccount(Account account) {
        getToken().setAccount(account);
    }

    @Override
    public boolean isAccountCredible() {
        return getToken().isAccountCredible();
    }

    @Override
    public void setAccountCredible(boolean credible) {
        getToken().setAccountCredible(credible);
    }

    @Override
    public String getTokenId() {
        return getToken(false).getTokenId();
    }

    @Override
    public <T> T getAttribute(String key) {
        return getToken().getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Serializable attribute) {
        getToken().setAttribute(key, attribute);
    }

    @Override
    public void removeAttribute(String key) {
        getToken().removeAttribute(key);
    }

    @Override
    public Enumeration<String> attributeNames() {
        return getToken().attributeNames();
    }

    @Override
    public void flush() {
        getToken().flush();
    }
}
