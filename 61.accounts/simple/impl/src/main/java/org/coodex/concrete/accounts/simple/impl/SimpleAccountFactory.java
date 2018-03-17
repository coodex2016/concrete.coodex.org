package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.accounts.AccountIDImpl;
import org.coodex.concrete.common.AcceptableAccountFactory;
import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.AccountID;
import org.coodex.util.Profile;

import static org.coodex.concrete.accounts.AccountIDImpl.TYPE_SIMPLE;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccountFactory implements AcceptableAccountFactory<AccountIDImpl> {
    @Override
    public <ID extends AccountID> Account<ID> getAccountByID(ID id) {
        return (Account<ID>) new SimpleAccount((AccountIDImpl) id);
    }

    @Override
    public boolean accept(AccountIDImpl param) {
        boolean isSimple = param != null && param.getType() == TYPE_SIMPLE;

        return isSimple && Profile.getResource("accounts/" + param.getId() + ".properties") != null;
    }
}
