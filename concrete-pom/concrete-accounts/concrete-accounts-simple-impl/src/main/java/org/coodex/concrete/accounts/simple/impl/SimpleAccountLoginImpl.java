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

package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.accounts.TOTPAuthenticator;
import org.coodex.concrete.accounts.simple.api.Login;
import org.coodex.concrete.common.AccountsErrorCodes;
import org.coodex.concrete.common.ClassifiableAccountID;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Profile;

import javax.inject.Inject;

import static org.coodex.concrete.accounts.AccountConstants.TYPE_SIMPLE;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccountLoginImpl implements Login {

    private Profile profile = Profile.get("simpleAccounts");

    @Inject
    private SimpleAccountFactory accountFactory;

    private Token token = TokenWrapper.getInstance();

    @Override
    public String login(String account, String password, String authCode) {

        ClassifiableAccountID accountId = new ClassifiableAccountID(TYPE_SIMPLE, account);
        IF.not(accountFactory.accept(accountId), AccountsErrorCodes.NONE_THIS_ACCOUNT);

        SimpleAccount simpleAccount = (SimpleAccount) accountFactory.getAccountByID(accountId);
        if (profile.getBool("password", true)) {
            IF.is(password == null || !password.equals(simpleAccount.getPassword()),
                    AccountsErrorCodes.LOGIN_FAILED);
        }

        if (profile.getBool("authCode", true)) {
            IF.is(authCode == null || !TOTPAuthenticator.authenticate(
                            authCode, simpleAccount.getAuthKey()),
                    AccountsErrorCodes.LOGIN_FAILED);
        }

        token.setAccount(simpleAccount);
        token.setAccountCredible(true);
        return "ok";
    }

    @Override
    public void logout() {
        token.invalidate();
    }
}
