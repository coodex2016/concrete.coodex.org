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

/**
 * Created by davidoff shen on 2017-05-03.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "t_concrete_accounts_organization")
@Inheritance
@DiscriminatorColumn(name = "organization_type")
public class OrganizationEntity extends AbstractEntity {


    @Column(length = 2000)
    private String description;

    private String domain;

    @Column(updatable = false, insertable = false)
    private String higherLevelId;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "higherLevelId")
    private OrganizationEntity higherLevel;

    public OrganizationEntity getHigherLevel() {
        return higherLevel;
    }

    public void setHigherLevel(OrganizationEntity higherLevel) {
        this.higherLevel = higherLevel;
    }

    public String getHigherLevelId() {
        return higherLevelId;
    }
//
//    public void setHigherLevelId(String higherLevelId) {
//        this.higherLevelId = higherLevelId;
//    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

}
