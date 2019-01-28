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

package org.coodex.concrete.core.token.sharedcache;

import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.AbstractToken;
import org.coodex.concurrent.Coalition;
import org.coodex.concurrent.Debouncer;
import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.util.Clock;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class SharedCacheToken /*implements Token*/ extends AbstractToken {

    private static final String PREFIX = SharedCacheToken.class.getCanonicalName();
    private Data tokenData;
    private SharedCacheClient client;
    private String tokenId;
    private long maxIdleTime;
    private String cacheKey;

    private Debouncer<Runnable> debouncer = new Debouncer<Runnable>(new Coalition.Callback<Runnable>() {
        @Override
        public void call(Runnable runnable) {
            runnable.run();
        }
    },10, ConcreteHelper.getScheduler("sct.debouncer"));

    SharedCacheToken(SharedCacheClient client, String tokenId, long maxIdleTime) {
        this.client = client;
        this.tokenId = tokenId;
        this.maxIdleTime = maxIdleTime;
        this.cacheKey = PREFIX + "." + this.tokenId;
        init();
        runListeners(Event.CREATED, false);
    }

    private void write() {
        debouncer.call(new Runnable() {
            @Override
            public void run() {
                client.put(cacheKey, tokenData, maxIdleTime);
            }
        });
    }

    private synchronized void init() {
        if (tokenData == null) {
            tokenData = client.get(cacheKey);
            if (tokenData == null) {
                tokenData = new Data();
            }
        }
        write();
    }

    @Override
    public long created() {
        return tokenData.created;
    }

    @Override
    public boolean isValid() {
        return tokenData.valid;
    }

    @Override
    protected void $invalidate() {
        tokenData.valid = false;
        tokenData.map.clear();
        client.remove(cacheKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends AccountID> Account<ID> currentAccount() {
        return tokenData.currentAccountId == null ? null :
                BeanProviderFacade.getBeanProvider().getBean(AccountFactory.class).getAccountByID((ID) tokenData.currentAccountId);
    }

    private boolean sameAccount(Account account) {
        if (tokenData.currentAccountId == null && account == null) return true;
        if (tokenData.currentAccountId == null || account == null) return false;
        return tokenData.currentAccountId.equals(account.getId());

    }

    @Override
    public void setAccount(Account account) {
        if (!sameAccount(account)) {
            tokenData.currentAccountId = account == null ? null : account.getId();
            write();
        }
    }

    @Override
    public boolean isAccountCredible() {
        return tokenData.currentAccountId == null ? false : tokenData.accountCredible;
    }

    @Override
    public void setAccountCredible(boolean credible) {
        if (tokenData.accountCredible != credible) {
            tokenData.accountCredible = credible;
            write();
        }
    }

    @Override
    public String getTokenId() {
        return tokenId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> tClass) {
        return (T) tokenData.map.get(key);
    }

    @Override
    public void setAttribute(String key, Serializable attribute) {
        if (isValid()) {
            tokenData.map.put(key, attribute);
            write();
        }
    }

    @Override
    public void removeAttribute(String key) {
        if (isValid()) {
            tokenData.map.remove(key);
            write();
        }
    }

    @Override
    public Enumeration<String> attributeNames() {
        return new Vector<String>(tokenData.map.keySet()).elements();
    }

    @Override
    public void flush() {
        write();
    }

    @Override
    protected void $renew() {
        tokenData.valid = true;
        write();
    }

    @Override
    public String toString() {
        return "SharedCacheToken{" +
                "tokenData=" + tokenData +
                ", client=" + client +
                ", tokenId='" + tokenId + '\'' +
                ", maxIdleTime=" + maxIdleTime +
                ", cacheKey='" + cacheKey + '\'' +
                '}';
    }

    static class Data implements Serializable {
        long created = Clock.currentTimeMillis();
        boolean valid = true;
        Serializable currentAccountId = null;
        boolean accountCredible = false;
        HashMap<String, Serializable> map = new HashMap<String, Serializable>();

        @Override
        public String toString() {
            return "Data{" +
                    "created=" + created +
                    ", valid=" + valid +
                    ", currentAccountId=" + currentAccountId +
                    ", accountCredible=" + accountCredible +
                    ", map=" + map +
                    '}';
        }
    }
}
