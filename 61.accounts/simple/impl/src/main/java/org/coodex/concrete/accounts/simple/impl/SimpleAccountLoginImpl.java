package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.accounts.AccountIDImpl;
import org.coodex.concrete.accounts.TOTPAuthenticator;
import org.coodex.concrete.accounts.simple.api.Login;
import org.coodex.concrete.common.AccountsErrorCodes;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Profile;

import javax.inject.Inject;

import static org.coodex.concrete.accounts.AccountIDImpl.TYPE_SIMPLE;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccountLoginImpl implements Login {

    private Profile profile = Profile.getProfile("simpleAccounts.properties");

    @Inject
    private SimpleAccountFactory accountFactory;

    private Token token = TokenWrapper.getInstance();

    @Override
    public String login(String account, String password, String authCode) {

        AccountIDImpl accountId = new AccountIDImpl(TYPE_SIMPLE, account);
        Assert.not(accountFactory.accept(accountId), AccountsErrorCodes.NONE_THIS_ACCOUNT);

        SimpleAccount simpleAccount = (SimpleAccount) accountFactory.getAccountByID(accountId);
        if (profile.getBool("password", true)) {
            Assert.is(password == null || !password.equals(simpleAccount.getPassword()),
                    AccountsErrorCodes.LOGIN_FAILED);
        }

        if (profile.getBool("authCode", true)) {
            Assert.is(authCode == null || !TOTPAuthenticator.authenticate(
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
