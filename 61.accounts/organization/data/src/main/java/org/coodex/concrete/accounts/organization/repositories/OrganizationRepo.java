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

package org.coodex.concrete.accounts.organization.repositories;

import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by davidoff shen on 2017-05-08.
 */
public interface OrganizationRepo extends CrudRepository<OrganizationEntity, String> {

    List<OrganizationEntity> findByTenantAndHigherLevelId(String tenant, String higherLevelId);

    OrganizationEntity findOneByTenantAndNameAndHigherLevelId(String tenant, String name, String higherLevelId);

    OrganizationEntity findOneByTenantAndNameAndHigherLevelIdAndIdNot(String tenant, String name, String higherLevelId, String id);

//    Long countByHigherLevelId(String higherLevelId);
}
