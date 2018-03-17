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

import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Created by davidoff shen on 2017-05-08.
 */
@NoRepositoryBean
public interface AbstractPersonAccountRepo<P extends AbstractPersonAccountEntity>
        extends CrudRepository<P, String>, JpaSpecificationExecutor<P> {

    long countByCellphoneAndTenant(String cellPhone, String tenant);

    long countByIdCardNoAndTenant(String idCardNo, String tenant);

    long countByEmailAndTenant(String email, String tenant);

    P findFirstByCellphoneAndTenant(String cellPhone, String tenant);

    P findFirstByIdCardNoAndTenant(String idCardNo, String tenant);

    P findFirstByEmailAndTenant(String email, String tenant);

}
