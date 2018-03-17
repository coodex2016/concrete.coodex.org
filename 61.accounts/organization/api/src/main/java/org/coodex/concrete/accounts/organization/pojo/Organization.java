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

package org.coodex.concrete.accounts.organization.pojo;

import org.coodex.concrete.api.Description;
import org.coodex.concrete.accounts.AbstractPojo;

/**
 * Created by davidoff shen on 2017-04-28.
 */
public abstract class Organization extends AbstractPojo {
    public static final int TYPE_INSTITUTION = 1;
    public static final int TYPE_DEPARTMENT = 2;
    private String description;
    private String domain;
    private int type;



    @Description(
            name = "描述",
            description = "用以说明该组织的职能等"
    )
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Description(name = "组织领域", description = "可用于RBAC的领域")
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Description(
            name = "组织类型",
            description = "方便客户端识别具体的组织类型，可扩展，目前，单位类型为1，部门类型为2"
    )
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
