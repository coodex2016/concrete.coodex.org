/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.AccountID;


/**
 * Created by davidoff shen on 2017-05-09.
 */
public class AccountIDImpl implements AccountID {
    public static final int TYPE_ADMINISTRATOR = 0;
    public static final int TYPE_ORGANIZATION = 1;
    public static final int TYPE_TENANT_ADMINISTRATOR = 2;

    public static final int TYPE_SIMPLE = 99999;

    private int type;

    private String id;

    public AccountIDImpl() {
    }

    public AccountIDImpl(int type, String id) {
        this.type = type;
        this.id = id;
    }

    /**
     * @return 账户类型，0为系统管理员，1为组织结构模型账户，其他待扩展
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return 账户id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String serialize() {
        return AccountIDImplDeserializer.serialize(this);
    }
}
