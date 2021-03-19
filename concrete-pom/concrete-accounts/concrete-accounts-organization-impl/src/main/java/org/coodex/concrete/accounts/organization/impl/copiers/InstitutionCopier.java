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

package org.coodex.concrete.accounts.organization.impl.copiers;

import org.coodex.concrete.accounts.organization.entities.AbstractInstitutionEntity;
import org.coodex.concrete.accounts.organization.pojo.Institution;

import static org.coodex.concrete.accounts.organization.pojo.Organization.TYPE_INSTITUTION;

/**
 * Created by davidoff shen on 2017-05-11.
 */
public abstract class InstitutionCopier<T extends Institution, E extends AbstractInstitutionEntity>
        extends OrganizationCopier<T, E> {

    @Override
    public E copyA2B(T t, E e) {
        e.setAddress(t.getAddress());
        e.setCode(t.getCode());
        return super.copyA2B(t, e);
    }

    @Override
    public T copyB2A(E e, T t) {
        t.setType(TYPE_INSTITUTION);
        t.setAddress(e.getAddress());
        t.setCode(e.getCode());
        return super.copyB2A(e, t);
    }
}
