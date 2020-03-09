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

package org.coodex.concrete.accounts.organization.entities;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by davidoff shen on 2017-05-19.
 */
@Entity
@Table(name = "t_org_account_login_cache_entry")
public class LoginCacheEntryEntity implements Serializable {

    @Id // 每个账户仅保留一条记录
    private String accountId;

    @Column(nullable = false, unique = true, updatable = false)
    private String credential;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar validation;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastLogin;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public Calendar getValidation() {
        return validation;
    }

    public void setValidation(Calendar validation) {
        this.validation = validation;
    }

    public Calendar getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Calendar lastLogin) {
        this.lastLogin = lastLogin;
    }
}
