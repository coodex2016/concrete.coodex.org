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
import org.coodex.concrete.common.Token;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Created by davidoff shen on 2017-05-20.
 */
class ReadOnlyToken implements Token {
    private final Token token;

    ReadOnlyToken(Token token) {
        this.token = token;
    }

    @Override
    public long created() {
        return token.created();
    }

    @Override
    public boolean isValid() {
        return token.isValid();
    }

    @Override
    public void invalidate() {
        throw new RuntimeException("cannot invalidate in listener.");
    }

    @Override
    public Account currentAccount() {
        return token.currentAccount();
    }

    @Override
    public void setAccount(Account account) {
        throw new RuntimeException("cannot set account in listener.");
    }

    @Override
    public boolean isAccountCredible() {
        return token.isAccountCredible();
    }

    @Override
    public void setAccountCredible(boolean credible) {
        throw new RuntimeException("cannot set account credible in listener.");
    }

    @Override
    public String getTokenId() {
        return token.getTokenId();
    }

    @Override
    @Deprecated
    public <T> T getAttribute(String key) {
        return token.getAttribute(key);
    }

    @Override
    public <T> T getAttribute(String key, Class<T> clz) {
        return token.getAttribute(key, clz);
    }

    @Override
    public void setAttribute(String key, Serializable attribute) {
        throw new RuntimeException("cannot set attribute in listener.");
    }

    @Override
    public void removeAttribute(String key) {
        throw new RuntimeException("cannot remove attribute in listener");
    }

    @Override
    public Enumeration<String> attributeNames() {
        return token.attributeNames();
    }

    @Override
    public void flush() {
        throw new RuntimeException("cannot flust in listener");
    }

    @Override
    public void renew() {
        throw new RuntimeException("cannot renew in listener");
    }
}
