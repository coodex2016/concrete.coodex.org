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

import org.coodex.concrete.accounts.AuthorizableEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-05-03.
 */
@MappedSuperclass
public abstract class AbstractPositionEntity extends AbstractEntity
        implements AuthorizableEntity {

    @Column(updatable = false, insertable = false)
    private String belong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belong")
    private OrganizationEntity belongTo;

    @ElementCollection
    @CollectionTable(name = "c_position_role", joinColumns = @JoinColumn(name = "position"))
    @Column(name = "role")
    private Set<String> roles;

    public OrganizationEntity getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(OrganizationEntity belongTo) {
        this.belongTo = belongTo;
    }
//
//    public void setBelong(String belong) {
//        this.belong = belong;
//    }

    public String getBelong() {
        return belong;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
