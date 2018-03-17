package org.coodex.concrete.accounts;

import org.coodex.concrete.common.AccountID;
import org.coodex.concrete.common.AccountIDDeserializer;

/**
 * Created by davidoff shen on 2017-07-17.
 */
public class AccountIDImplDeserializer implements AccountIDDeserializer {

    private static final String START_WITH = "CONCRETE-ACCOUNTS:";
    private static final String SPLIT_BY = ",";


    final static String serialize(AccountIDImpl accountID) {
        return String.format("%s%d%s%s", START_WITH, accountID.getType(), SPLIT_BY, accountID.getId());
    }


    @Override
    public AccountID deserialize(String accountIDStr) {
        int type = 0;
        String id = null;
        accountIDStr = accountIDStr.substring(START_WITH.length());
        int i = accountIDStr.indexOf(SPLIT_BY);
        type = Integer.valueOf(accountIDStr.substring(0, i));
        id = accountIDStr.substring(i + SPLIT_BY.length());
        return new AccountIDImpl(type, id);
    }

    @Override
    public boolean accept(String accountIDStr) {
        return accountIDStr != null && accountIDStr.startsWith(START_WITH);
    }
}
