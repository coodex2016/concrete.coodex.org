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

package org.coodex.concrete.accounts.organization.impl.copiers;

import org.coodex.concrete.accounts.organization.entities.AbstractDepartmentEntity;
import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.accounts.organization.pojo.Organization;

/**
 * Created by davidoff shen on 2017-05-11.
 */
public abstract class DepartmentCopier<T extends Department, E extends AbstractDepartmentEntity>
        extends OrganizationCopier<T, E>{

    @Override
    public E copyA2B(T t, E e) {
        return super.copyA2B(t, e);
    }

    @Override
    public T copyB2A(E e, T t) {
        t.setType(Organization.TYPE_DEPARTMENT);
        return super.copyB2A(e, t);
    }
}
