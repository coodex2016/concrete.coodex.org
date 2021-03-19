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

/**
 * Created by davidoff shen on 2017-05-27.
 */
public class TenantQuery {
    private String nameLike;
    private String accountNameLike;
//    private String createdLessThen;
//    private String createdGatherThen;

    private Boolean using;

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    public String getAccountNameLike() {
        return accountNameLike;
    }

    public void setAccountNameLike(String accountNameLike) {
        this.accountNameLike = accountNameLike;
    }

//    public String getCreatedLessThen() {
//        return createdLessThen;
//    }

//    public void setCreatedLessThen(String createdLessThen) {
//        this.createdLessThen = createdLessThen;
//    }
//
//    public String getCreatedGatherThen() {
//        return createdGatherThen;
//    }
//
//    public void setCreatedGatherThen(String createdGatherThen) {
//        this.createdGatherThen = createdGatherThen;
//    }

    public Boolean isUsing() {
        return using;
    }

    public void setUsing(Boolean using) {
        this.using = using;
    }
}
