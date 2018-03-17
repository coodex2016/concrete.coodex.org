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

package org.coodex.concrete.accounts.organization.impl;

import org.coodex.concrete.accounts.organization.api.AbstractPositionManagementService;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.OrganizationErrorCodes;
import org.coodex.concrete.common.TwoWayCopier;
import org.coodex.util.Common;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteContext.putLoggingData;

/**
 * Created by davidoff shen on 2017-05-10.
 */
@Transactional(rollbackOn = Throwable.class)
public abstract class AbstractPositionManagementServiceImpl
        <J extends Position,
                E extends AbstractPositionEntity,
                P extends AbstractPersonAccountEntity<E>>
        extends AbstractManagementService<E, P>
        implements AbstractPositionManagementService<J> {

    @Inject
    protected TwoWayCopier<J, E> positionCopier;

    @Override
    public StrID<J> save(J position, String belong) {
        checkManagementPermission(belong);
        E positionEntity = positionCopier.copyA2B(position);
        positionEntity.setBelongTo(checkBelongToExists(belong));

        putLoggingData("new", positionEntity);
        return new StrID<J>(positionEntity.getId(),
                positionCopier.copyB2A(positionRepo.save(positionEntity), position));
    }

    protected E getPositionEntity(String id) {
        return Assert.isNull(positionRepo.findOne(id), OrganizationErrorCodes.POSITION_NOT_EXISTS);
    }

    protected E getPositionWithPermissionCheck(String id) {
        E positionEntity = getPositionEntity(id);
        checkManagementPermission(positionEntity.getBelong());
        return positionEntity;
    }

    @Override
    public void update(String id, J position) {
        E positionEntity = getPositionWithPermissionCheck(id);
//        checkManagementPermission(positionEntity.getBelong());
        putLoggingData("old", deepCopy(positionEntity));
        putLoggingData("new", positionRepo.save(positionCopier.copyA2B(position, positionEntity)));
    }

    @Override
    public void updateBelongTo(String id, String belong) {
        E positionEntity = getPositionWithPermissionCheck(id);
//        checkManagementPermission(positionEntity.getBelong());
        checkManagementPermission(belong);
        OrganizationEntity organizationEntity = checkBelongToExists(belong);
        if (!positionEntity.getBelongTo().getId().equals(belong)) {
            putLoggingData("target", organizationEntity);
            putLoggingData("original", positionEntity.getBelongTo());

            positionEntity.setBelongTo(organizationEntity);
            positionRepo.save(positionEntity);
        }

    }

    @Override
    public void updateOrder(String id, Integer order) {
        E positionEntity = getPositionWithPermissionCheck(id);
        updateOrder(order, positionEntity, positionRepo);
    }

    @Override
    public void delete(String id) {
        putLoggingData("deleted", deletePosition(getPositionWithPermissionCheck(id)));
    }

    @Override
    public void grantTo(String id, String[] roles) {
        grantTo(getPositionWithPermissionCheck(id), positionRepo, roles);
    }

    @Override
    public Set<String> roles(String id) {
        return Common.join(getPositionWithPermissionCheck(id).getRoles());
    }
}
