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

import org.coodex.concrete.accounts.AccountsCommon;
import org.coodex.concrete.accounts.organization.api.AbstractPersonManagementService;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.OrganizationErrorCodes;
import org.coodex.copier.TwoWayCopier;
import org.coodex.util.Common;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.accounts.AccountsCommon.getTenant;
import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.coodex.concrete.common.OrganizationErrorCodes.PERSON_NOT_EXISTS;
import static org.coodex.concrete.common.OrganizationErrorCodes.POSITION_NOT_EXISTS;

/**
 * Created by davidoff shen on 2017-05-10.
 */
public abstract class AbstractPersonManagementServiceImpl
        <J extends AbstractPositionEntity,
                E extends AbstractPersonAccountEntity<J>, P extends Person>
        extends AbstractManagementService<J, E>
        implements AbstractPersonManagementService<P> {

    @Inject
    protected TwoWayCopier<P, E> personCopier;

    @Override
    public StrID<P> save(P person, String[] positions) {
        if (person.getCellphone() != null) {
            IF.is(personAccountRepo.countByCellphoneAndTenant(person.getCellphone(), getTenant()) != 0, OrganizationErrorCodes.CELL_PHONE_EXISTS);
        }
        if (person.getIdCardNo() != null) {
            IF.is(personAccountRepo.countByIdCardNoAndTenant(person.getIdCardNo(), getTenant()) != 0, OrganizationErrorCodes.ID_CARD_NO_EXISTS);
        }
        if (person.getEmail() != null) {
            IF.is(personAccountRepo.countByEmailAndTenant(person.getEmail(), getTenant()) != 0, OrganizationErrorCodes.EMAIL_EXISTS);
        }
        E personEntity = personCopier.copyA2B(person);
        personEntity.setPositions(getPositionsWithPermissionCheck(positions));
        person = personCopier.copyB2A(personAccountRepo.save(personEntity), person);
        putLoggingData("new", person);
        return new StrID<P>(personEntity.getId(), person);
    }

    protected Set<J> getPositionsWithPermissionCheck(String[] positions) {
        Set<J> positionEntities = new HashSet<J>();
        for (String positionId : positions) {
            J positionEntity = IF.isNull(getPositionRepo().findById(positionId).orElse(null), POSITION_NOT_EXISTS);
            checkManagementPermission(positionEntity.getBelong());
            positionEntities.add(positionEntity);
        }
        return positionEntities;
    }


    protected E getPersonEntity(String id) {
        return IF.isNull(personAccountRepo.findById(id).orElse(null), PERSON_NOT_EXISTS);
    }


    @Override
    public void update(String id, P person) {
        E personEntity = getPersonEntityWithPermissionCheck(id);
        if (!Common.isSameStr(person.getCellphone(), personEntity.getCellphone()) && person.getCellphone() != null) {
            IF.is(personAccountRepo.countByCellphoneAndTenant(person.getCellphone(), getTenant()) != 0, OrganizationErrorCodes.CELL_PHONE_EXISTS);
        }
        if (!Common.isSameStr(person.getIdCardNo(), personEntity.getIdCardNo()) && person.getIdCardNo() != null) {
            IF.is(personAccountRepo.countByIdCardNoAndTenant(person.getIdCardNo(), getTenant()) != 0, OrganizationErrorCodes.ID_CARD_NO_EXISTS);
        }
        if (!Common.isSameStr(person.getEmail(), personEntity.getEmail()) && person.getEmail() != null) {
            IF.is(personAccountRepo.countByEmailAndTenant(person.getEmail(), getTenant()) != 0, OrganizationErrorCodes.EMAIL_EXISTS);
        }
        E old = deepCopy(personEntity);
        putLoggingData("old", deepCopy(personEntity));
        putLoggingData("new", personAccountRepo.save(personCopier.copyA2B(person, personEntity)));
    }

    protected E getPersonEntityWithPermissionCheck(String id) {
        E personEntity = getPersonEntity(id);
        for (J position : personEntity.getPositions()) {
            checkManagementPermission(position.getBelong());
        }
        return personEntity;
    }

    @Override
    public void updatePositions(String id, String[] positions) {

        E personEntity = getPersonEntityWithPermissionCheck(id);
        putLoggingData("old", deepCopy(personEntity));
        personEntity.setPositions(getPositionsWithPermissionCheck(positions));
        putLoggingData("new", personAccountRepo.save(personEntity));
    }

    @Override
    public void updateOrder(String id, Integer order) {
        updateOrder(order, getPersonEntityWithPermissionCheck(id), personAccountRepo);
    }

    @Override
    public void delete(String id) {
        E personEntity = getPersonEntityWithPermissionCheck(id);
        E old = deepCopy(personEntity);
        personEntity.setPositions(null);
        personAccountRepo.delete(personAccountRepo.save(personEntity));
        putLoggingData("deleted", old);
    }

    @Override
    public void grantTo(String id, String[] roles) {
        grantTo(getPersonEntityWithPermissionCheck(id), personAccountRepo, roles);
    }

    @Override
    public Set<String> personRoles(String id) {
        return Common.join(getPersonEntityWithPermissionCheck(id).getRoles());
    }

    @Override
    public Set<String> allRoles(String id) {
        E personEntity = getPersonEntityWithPermissionCheck(id);
        Set<String> roles = Common.join(personEntity.getRoles());
        for (J position : personEntity.getPositions()) {
            roles.addAll(position.getRoles());
        }
        return roles;
    }

    @Override
    public void resetPassword(String id) {
        AccountsCommon.resetPassword(getPersonEntityWithPermissionCheck(id), personAccountRepo);
    }


    @Override
    public void resetAuthCode(String id) {
        AccountsCommon.resetAuthCode(getPersonEntityWithPermissionCheck(id), personAccountRepo);
//        E entity = getPersonEntityWithPermissionCheck(id);
//        entity.setAuthCodeKey(null);
//        entity.setAuthCodeKeyActiveTime(null);
//        personAccountRepo.save(entity);
//        putLoggingData("authCode", "reset");
    }
}
