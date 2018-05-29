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

import org.coodex.commons.jpa.springdata.SpecCommon;
import org.coodex.concrete.accounts.AuthorizableEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.coodex.concrete.accounts.organization.repositories.AbstractPositionRepo;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.IF;
import org.coodex.util.Common;
import org.springframework.data.repository.CrudRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;

import static org.coodex.concrete.accounts.AccountsCommon.getTenant;
import static org.coodex.concrete.accounts.organization.entities.AbstractEntity.DEFAULT_ORDER;
import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.coodex.concrete.common.OrganizationErrorCodes.POSITION_CANNOT_DELETE;

/**
 * Created by davidoff shen on 2017-05-09.
 */
@Transactional(rollbackOn = Throwable.class)
public abstract class AbstractManagementService<
        J extends AbstractPositionEntity,
        P extends AbstractPersonAccountEntity<J>>
        extends AbstractOrgService<J, P> {

    @Inject
    private AbstractPositionRepo<J> positionRepo;

    public AbstractPositionRepo<J> getPositionRepo() {
        return positionRepo;
    }

    protected <O extends Serializable> O deepCopy(O value) {
        try {
            return Common.deepCopy(value);
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }


    protected <O extends AbstractEntity, R extends CrudRepository<O, String>> void updateOrder(
            Integer order, O entity, R repo) {

        if (order == null) order = DEFAULT_ORDER;
        if (entity.getDisplayOrder().intValue() != order.intValue()) {
            putLoggingData("object", entity);
            putLoggingData("original", entity.getDisplayOrder());
            entity.setDisplayOrder(order);
            repo.save(entity);
            putLoggingData("target", order);
        }
    }


    protected J deletePosition(J positionEntity) {
        IF.is(personAccountRepo.count(
                SpecCommon.<P, J>memberOf("positions", positionEntity)) > 0,
                POSITION_CANNOT_DELETE);
        positionRepo.delete(positionEntity);
        return positionEntity;
    }


    protected Collection<AbstractEntity> deleteOrganization(OrganizationEntity entity) {

        List<AbstractEntity> deletedEntities = new ArrayList<AbstractEntity>();

        //删除所有职位
        for (J position : positionRepo.findByBelong(entity.getId())) {
            deletedEntities.add(deletePosition(position));
        }

        //删除所有组织
        for (OrganizationEntity organizationEntity :
                organizationRepo.findByTenantAndHigherLevelId(getTenant(), entity.getId())) {
            deletedEntities.addAll(deleteOrganization(organizationEntity));
        }
        organizationRepo.delete(entity);
        deletedEntities.add(entity);
        return deletedEntities;
    }

    protected <E extends AuthorizableEntity, R extends CrudRepository<E, String>> void grantTo(E entity, R repo, String[] roles) {
        Set<String> original = Common.join(entity.getRoles());
        Set<String> target = roles == null || roles.length == 0 ?
                new HashSet<String>() : new HashSet<String>(Arrays.asList(roles));
        if (Common.difference(target, original).size() > 0 ||
                Common.difference(original, target).size() > 0) {

            entity.setRoles(target);
            putLoggingData("object", repo.save(entity));
            putLoggingData("original", original);
            putLoggingData("target", target);
        }

    }

}
