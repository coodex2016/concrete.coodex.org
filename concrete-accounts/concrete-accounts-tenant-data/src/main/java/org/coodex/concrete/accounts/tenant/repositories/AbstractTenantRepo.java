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

package org.coodex.concrete.accounts.tenant.repositories;

import org.coodex.concrete.accounts.tenant.entities.AbstractTenantEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by davidoff shen on 2017-05-25.
 */
@NoRepositoryBean
public interface AbstractTenantRepo<T extends AbstractTenantEntity>
        extends CrudRepository<T, String>,
        JpaSpecificationExecutor<T>, PagingAndSortingRepository<T, String> {

    T findFirstByAccountName(String accountName);
}
