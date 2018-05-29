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

package org.coodex.concrete.accounts;

import org.coodex.concrete.common.NamedAccount;
import org.coodex.concrete.common.SaaSAccount;

import java.util.Set;

/**
 * Created by davidoff shen on 2017-05-26.
 */
public class TenantAccount implements SaaSAccount<AccountIDImpl>, NamedAccount<AccountIDImpl> {

    private String name;
    private String tenant;
    private String appSet;
    private AccountIDImpl id;
    private boolean valid = true;
    private Set<String> roles;


//    private

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @Override
    public AccountIDImpl getId() {
        return id;
    }

    public void setId(AccountIDImpl id) {
        this.id = id;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getAppSet() {
        return appSet;
    }

    public void setAppSet(String appSet) {
        this.appSet = appSet;
    }
}
