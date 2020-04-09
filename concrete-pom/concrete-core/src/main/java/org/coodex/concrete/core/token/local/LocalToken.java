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

package org.coodex.concrete.core.token.local;

import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.AccountFactory;
import org.coodex.concrete.common.BeanServiceLoaderProvider;
import org.coodex.concrete.core.token.AbstractToken;
import org.coodex.util.Clock;
import org.coodex.util.Common;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.coodex.util.Common.cast;

/**
 * Created by davidoff shen on 2016-09-05.
 */
@SuppressWarnings("unused")
class LocalToken /*implements Token*/ extends AbstractToken {

//    private final static Logger log = LoggerFactory.getLogger(LocalToken.class);


    private Map<String, Object> attributes = new HashMap<>();
    //    private Account currentAccount = null;
    private Serializable currentAccountId = null;
    private boolean accountCredible = false;

    private boolean valid = true;
    private long lastActive;
    private String sessionId = Common.getUUIDStr();
    private long created = Clock.currentTimeMillis();

    public LocalToken(String sessionId) {
        if (sessionId != null)
            this.sessionId = sessionId;
        active();
        runListeners(Event.CREATED, false);
    }

    void active() {
        lastActive = Clock.currentTimeMillis();
    }

    public long getLastActive() {
        return lastActive;
    }

    @Override
    public long created() {
        return created;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    protected void $invalidate() {
        attributes.clear();
        currentAccountId = null;
        accountCredible = false;
//        currentAccount = null;
        valid = false;
    }


    @Override
    public Account<?> currentAccount() {
        if (currentAccountId == null)
            return null;
        AccountFactory<?> accountFactory = BeanServiceLoaderProvider.getBeanProvider().getBean(AccountFactory.class);
        return accountFactory.getAccountByID(cast(currentAccountId));
    }

    @Override
    public void setAccount(Account<?> account) {
        currentAccountId = account.getId();
//        currentAccount = account;
    }

    @Override
    public boolean isAccountCredible() {
        return currentAccountId != null && accountCredible;
    }

    @Override
    public void setAccountCredible(boolean credible) {
        accountCredible = credible;
    }

//    @Override
//    public <ID> Account<ID> currentAccount() {
//        return currentAccount;
//    }
//
//    @Override
//    public void setAccount(Account account) {
//        currentAccount = account;
//    }

    @Override
    public String getTokenId() {
        return sessionId;
    }


    @Override
    public <T> T getAttribute(String key, Class<T> tClass) {
        return cast(attributes.get(key));
    }

    @Override
    public void setAttribute(String key, Serializable attribute) {
        attributes.put(key, attribute);
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public Enumeration<String> attributeNames() {
        return new Vector<>(attributes.keySet()).elements();
    }

    @Override
    public void flush() {
    }

    @Override
    protected void $renew() {
        if (!valid)
            valid = true;
    }

    @Override
    public String toString() {
        return "LocalToken{" +
                "attributes=" + attributes +
                ", currentAccountId=" + currentAccountId +
                ", accountCredible=" + accountCredible +
                ", valid=" + valid +
                ", lastActive=" + lastActive +
                ", sessionId='" + sessionId + '\'' +
                ", created=" + created +
                '}';
    }
}
