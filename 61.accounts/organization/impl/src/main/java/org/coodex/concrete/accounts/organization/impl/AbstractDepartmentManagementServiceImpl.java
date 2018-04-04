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

package org.coodex.concrete.accounts.organization.impl;

import org.coodex.concrete.accounts.organization.api.AbstractDepartmentManagementService;
import org.coodex.concrete.accounts.organization.entities.AbstractDepartmentEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.accounts.organization.repositories.AbstractDepartmentRepo;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.TwoWayCopier;
import org.coodex.util.Common;

import javax.inject.Inject;

import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.coodex.concrete.common.OrganizationErrorCodes.NONE_THIS_DEPARTMENT;

/**
 * Created by davidoff shen on 2017-05-09.
 */
public abstract class AbstractDepartmentManagementServiceImpl
        <D extends Department, E extends AbstractDepartmentEntity,
                J extends AbstractPositionEntity, P extends AbstractPersonAccountEntity<J>>
        extends AbstractManagementService<J, P>
        implements AbstractDepartmentManagementService<D> {

    @Inject
    protected AbstractDepartmentRepo<E> departmentRepo;

    @Inject
    protected TwoWayCopier<D, E> departmentCopier;


    protected E getDepartmentEntity(String id) {
        Assert.isNull(id, NONE_THIS_DEPARTMENT);
        return Assert.isNull(departmentRepo.findOne(id), NONE_THIS_DEPARTMENT);
    }


    @Override
    public StrID<D> save(D department, String higherLevel) {
        checkManagementPermission(higherLevel);

        checkDuplication(higherLevel, department.getName(), null);
        E departmentEntity = departmentCopier.copyA2B(department);
        departmentEntity.setHigherLevel(checkBelongToExists(higherLevel));

        putLoggingData("new", departmentEntity);
        return new StrID<D>(departmentEntity.getId(),
                departmentCopier.copyB2A(departmentRepo.save(departmentEntity), department));
    }

    @Override
    public void update(String id, D department) {
        checkManagementPermission(id);
        E departmentEntity = getDepartmentEntity(id);
        checkDuplication(departmentEntity.getHigherLevelId(), department.getName(), id);
        putLoggingData("old", deepCopy(departmentEntity));
        putLoggingData("new", departmentRepo.save(departmentCopier.copyA2B(department, departmentEntity)));
    }

    @Override
    public void updateHigherLevel(String id, String higherLevel) {
        checkManagementPermission(id);
        checkManagementPermission(higherLevel);
        OrganizationEntity higherLevelEntity = checkBelongToExists(higherLevel);
        E departmentEntity = getDepartmentEntity(id);
        checkManagementPermission(departmentEntity.getHigherLevelId());

        if (!Common.sameString(higherLevel, departmentEntity.getHigherLevelId())) {
            putLoggingData("original", departmentEntity.getHigherLevel());
            departmentEntity.setHigherLevel(higherLevelEntity);
            departmentRepo.save(departmentEntity);
            putLoggingData("target", departmentEntity.getHigherLevel());
        }
    }

    @Override
    public void updateOrder(String id, Integer order) {
        checkManagementPermission(id);
        updateOrder(order, getDepartmentEntity(id), departmentRepo);
    }

    @Override
    public void delete(String id) {
        checkManagementPermission(id);
        putLoggingData("deleted", deleteOrganization(getDepartmentEntity(id)));
    }
}
