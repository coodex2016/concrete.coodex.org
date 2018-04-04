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

package org.coodex.concrete.accounts.tenant.entities;

import org.coodex.concrete.accounts.CanLoginEntity;
import org.coodex.util.Common;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by davidoff shen on 2017-05-25.
 */
@MappedSuperclass
public class AbstractTenantEntity implements Serializable, CanLoginEntity {

    @Id
    private String id = Common.getUUIDStr();

    @Column(nullable = false, updatable = false, unique = true)
    private String accountName;

    private String appSet;

    private String name;

    //是否启用
    private boolean using = true;

    //停用时的余量，当再度启用时，需要需要将余量加上启用时间
    private long surplus = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created = Calendar.getInstance();
    // 密码散列值
    private String password;
    // 二步验证的key
    private String authCodeKey;

    // 二步验证key的激活时间
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar authCodeKeyActiveTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar validation;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getAuthCodeKey() {
        return authCodeKey;
    }

    @Override
    public void setAuthCodeKey(String authCodeKey) {
        this.authCodeKey = authCodeKey;
    }

    @Override
    public Calendar getAuthCodeKeyActiveTime() {
        return authCodeKeyActiveTime;
    }

    @Override
    public void setAuthCodeKeyActiveTime(Calendar authCodeKeyActiveTime) {
        this.authCodeKeyActiveTime = authCodeKeyActiveTime;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    public long getSurplus() {
        return surplus;
    }

    public void setSurplus(long surplus) {
        this.surplus = surplus;
    }

    public Calendar getValidation() {
        return validation;
    }

    public void setValidation(Calendar validation) {
        this.validation = validation;
    }

    public String getAppSet() {
        return appSet;
    }

    public void setAppSet(String appSet) {
        this.appSet = appSet;
    }
}
