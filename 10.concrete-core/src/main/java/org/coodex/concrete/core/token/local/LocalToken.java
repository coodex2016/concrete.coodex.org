package org.coodex.concrete.core.token.local;

import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.AccountFactory;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.common.Token;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by davidoff shen on 2016-09-05.
 */
class LocalToken implements Token {

    private final static Logger log = LoggerFactory.getLogger(LocalToken.class);


    private Map<String, Object> attributes = new HashMap<String, Object>();
    //    private Account currentAccount = null;
    private Serializable currentAccountId = null;
    private boolean accountCredible = false;

    private boolean valid = true;
    private long lastActive;
    private String sessionId = Common.getUUIDStr();
    private Runnable onInvalid;
    private long created = System.currentTimeMillis();

    public LocalToken(String sessionId, Runnable onInvalid) {
        if (sessionId != null)
            this.sessionId = sessionId;
        this.lastActive = System.currentTimeMillis();
        this.onInvalid = onInvalid;
    }

    void active() {
        lastActive = System.currentTimeMillis();
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

    void _invalidate() {
        attributes.clear();
        currentAccountId = null;
        accountCredible = false;
//        currentAccount = null;
        valid = false;
    }

    @Override
    public void invalidate() {
        _invalidate();
        onInvalidate();
    }

    @Override
    public void onInvalidate() {
        if (onInvalid != null) {
            try {
                onInvalid.run();
            } catch (Throwable t) {
                log.warn("error occurred on LocalToken invalidate. {}: {}",
                        t.getClass().getCanonicalName(), t.getLocalizedMessage());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends Serializable> Account<ID> currentAccount() {

        return currentAccountId == null ? null :
                BeanProviderFacade.getBeanProvider().getBean(AccountFactory.class).getAccountByID((ID) currentAccountId);
//        return currentAccount;
    }

    @Override
    public void setAccount(Account account) {
        currentAccountId = account.getId();
//        currentAccount = account;
    }

    @Override
    public boolean isAccountCredible() {
        return currentAccountId == null ? false : accountCredible;
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
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
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
        return new Vector<String>(attributes.keySet()).elements();
    }

    @Override
    public void flush() {

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
                ", onInvalid=" + onInvalid +
                ", created=" + created +
                '}';
    }
}
