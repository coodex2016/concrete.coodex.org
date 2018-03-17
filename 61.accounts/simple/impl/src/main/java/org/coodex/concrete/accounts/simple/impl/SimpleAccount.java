package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.accounts.AccountIDImpl;
import org.coodex.concrete.common.NamedAccount;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccount implements NamedAccount<AccountIDImpl> {

    private final Profile profile;
    private final AccountIDImpl id;

    public SimpleAccount(AccountIDImpl id) {
        this.profile = Profile.getProfile("/accounts/" + id.getId() + ".properties");
        this.id = id;
    }

    @Override
    public AccountIDImpl getId() {
        return id;
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<String>(Common.arrayToSet(profile.getStrList("roles")));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getName() {
        return profile.getString("name");
    }


    public String getPassword() {
        return profile.getString("password");
    }

    public String getAuthKey() {
        return profile.getString("authKey");
    }
}
