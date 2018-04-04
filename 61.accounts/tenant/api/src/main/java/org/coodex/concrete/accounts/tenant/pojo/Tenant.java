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

package org.coodex.concrete.accounts.tenant.pojo;

import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.mockers.DateTime;
import org.coodex.concrete.accounts.AbstractPojo;

/**
 * Created by davidoff shen on 2017-05-25.
 */
public class Tenant extends AbstractPojo {
    @Description(name = "[RW]租户帐号", description = "登录用。仅在初始化状态允许修改")
    private String accountName; //租户帐号

    @Description(name = "[RW]租户所属应用集", description = "仅在初始化状态允许修改")
    private String appSet;

    @Description(name = "[R]是否是初始状态")
    private boolean init;//是否为初始状态

    @Description(name = "[R]是否使用中")
    private boolean using;

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    @DateTime
    @Description(name = "[R]有效期至")
    private String validation;

    public String getAppSet() {
        return appSet;
    }

    public void setAppSet(String appSet) {
        this.appSet = appSet;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }
}
