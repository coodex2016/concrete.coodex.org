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

import org.coodex.concrete.accounts.organization.api.AbstractInstitutionManagementService;
import org.coodex.concrete.accounts.organization.entities.AbstractInstitutionEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.repositories.AbstractInstitutionRepo;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.TwoWayCopier;
import org.coodex.util.Common;

import javax.inject.Inject;

import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.coodex.concrete.common.OrganizationErrorCodes.NONE_THIS_INSTITUTION;

/**
 * Created by davidoff shen on 2017-05-08.
 */
public abstract class AbstractInstitutionManagementServiceImpl
        <I extends Institution, E extends AbstractInstitutionEntity,
                J extends AbstractPositionEntity, P extends AbstractPersonAccountEntity<J>>
        extends AbstractManagementService<J, P>
        implements AbstractInstitutionManagementService<I> {

    @Inject
    protected TwoWayCopier<I, E> institutionCopier;

    @Inject
    protected AbstractInstitutionRepo<E> institutionRepo;

    @Override
    public StrID<I> save(I institution, String higherLevel) {

        checkManagementPermission(higherLevel);
        //检查上级单位是否存在
        getInstitutionEntityNullSafe(higherLevel);
        // 检查同一级下是否有重名
        checkDuplication(higherLevel, institution.getName(), null);

        // 复制一个entity
        E institutionEntity = institutionCopier.copyA2B(institution);
        institutionEntity.setHigherLevel(Common.isBlank(higherLevel) ? null : checkBelongToExists(higherLevel));

        putLoggingData("new", institutionEntity);
        return new StrID<I>(institutionEntity.getId(),
                institutionCopier.copyB2A(institutionRepo.save(institutionEntity), institution));
    }

    private E getInstitutionEntity(String id) {
        IF.isNull(id, NONE_THIS_INSTITUTION);
        return IF.isNull(institutionRepo.findOne(id), NONE_THIS_INSTITUTION);
    }

    @Override
    public void update(String id, I institution) {
        checkManagementPermission(id);
        E institutionEntity = getInstitutionEntity(id);
        // 重名检查
        checkDuplication(institutionEntity.getHigherLevelId(), institution.getName(), id);
        putLoggingData("old", deepCopy(institutionEntity));
        putLoggingData("new", institutionRepo.save(institutionCopier.copyA2B(institution, institutionEntity)));
    }

    private E getInstitutionEntityNullSafe(String id) {
        return id == null ? null : getInstitutionEntity(id);
    }


    @Override
    public void updateHigherLevel(String id, String higherLevel) {
        checkManagementPermission(id);
        checkManagementPermission(higherLevel);

        E higherLevelEntity = getInstitutionEntityNullSafe(higherLevel);

        E institutionEntity = getInstitutionEntity(id);
        checkManagementPermission(institutionEntity.getHigherLevelId());

        if (!Common.sameString(higherLevel, institutionEntity.getHigherLevelId())) {
            circleCheck(higherLevelEntity, id);

            OrganizationEntity originalEntity = institutionEntity.getHigherLevel();

            institutionEntity.setHigherLevel(Common.isBlank(higherLevel) ? null : checkBelongToExists(higherLevel));
            institutionRepo.save(institutionEntity);

            putLoggingData("original", originalEntity);
            putLoggingData("target", higherLevelEntity);
        }
    }

    @Override
    public void updateOrder(String id, Integer order) {
        checkManagementPermission(id);
        updateOrder(order, getInstitutionEntity(id), institutionRepo);
    }


    @Override
    public void delete(String id) {
        checkManagementPermission(id);
        putLoggingData("deleted", deleteOrganization(getInstitutionEntity(id)));
    }

}
