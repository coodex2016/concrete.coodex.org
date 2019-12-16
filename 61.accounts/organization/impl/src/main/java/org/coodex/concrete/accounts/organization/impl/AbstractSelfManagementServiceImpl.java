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

import org.coodex.concrete.accounts.AccountConstants;
import org.coodex.concrete.accounts.AccountsCommon;
import org.coodex.concrete.accounts.organization.api.AbstractSelfManagementService;
import org.coodex.concrete.accounts.organization.entities.*;
import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.accounts.organization.repositories.AbstractPersonAccountRepo;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.copier.TwoWayCopier;
import org.coodex.util.Common;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.coodex.concrete.accounts.AccountsCommon.checkAuthCode;
import static org.coodex.concrete.accounts.AccountsCommon.getAuthenticatorDesc;
import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.coodex.concrete.common.OrganizationErrorCodes.NOT_ORGANIZATION_ACCOUNT;

/**
 * Created by davidoff shen on 2017-05-18.
 */
public abstract class AbstractSelfManagementServiceImpl<
        I extends Institution, D extends Department,
        J extends Position, P extends Person,
        IE extends AbstractInstitutionEntity,
        DE extends AbstractDepartmentEntity,
        JE extends AbstractPositionEntity,
        PE extends AbstractPersonAccountEntity<JE>>
        implements AbstractSelfManagementService<I, D, J, P> {


    protected Token token = TokenWrapper.getInstance();

    @Inject
    protected AbstractPersonAccountRepo<PE> personAccountRepo;

    @Inject
    protected TwoWayCopier<I, IE> institutionCopier;

    @Inject
    protected TwoWayCopier<D, DE> departmentCopier;

    @Inject
    protected TwoWayCopier<J, JE> positionCopier;

    protected String getTenant() {
        return TenantBuilderWrapper.getInstance().getTenant();
    }

    //    @Override
    protected PE getCurrentAccountEntity() {
        Account<ClassifiableAccountID> currentAccount = token.currentAccount();
        IF.is(currentAccount.getId().getCategory() != AccountConstants.TYPE_ORGANIZATION, NOT_ORGANIZATION_ACCOUNT);
        return personAccountRepo.findById(currentAccount.getId().getId()).orElse(null);
    }

    @Override
    public List<StrID<I>> getMyInstitutions() {
        Set<String> institutionIdSet = new HashSet<String>();
        List<StrID<I>> institutions = new ArrayList<StrID<I>>();
        PE personEntity = getCurrentAccountEntity();
        for (JE position : personEntity.getPositions()) {
            IE institutionEntity = null;
            OrganizationEntity organizationEntity = position.getBelongTo();
            while (organizationEntity instanceof AbstractDepartmentEntity) {
                organizationEntity = organizationEntity.getHigherLevel();
                if (organizationEntity == null) break;
            }
            //noinspection unchecked
            institutionEntity = (IE) organizationEntity;
            while (institutionEntity != null) {
                if (!institutionIdSet.contains(institutionEntity.getId())) {
                    institutionIdSet.add(institutionEntity.getId());
                    institutions.add(new StrID<I>(institutionEntity.getId(), institutionCopier.copyB2A(institutionEntity)));
                }
                //noinspection unchecked
                institutionEntity = (IE) institutionEntity.getHigherLevel();
            }
        }
        return institutions;
    }

    @Override
    public List<StrID<D>> getMyDepartments() {
        Set<String> departmentIdSet = new HashSet<String>();
        List<StrID<D>> departments = new ArrayList<StrID<D>>();
        PE personEntity = getCurrentAccountEntity();
        for (JE position : personEntity.getPositions()) {
            OrganizationEntity organizationEntity = position.getBelongTo();
            while (organizationEntity instanceof AbstractDepartmentEntity) {
                //noinspection unchecked
                DE departmentEntity = (DE) organizationEntity;
                if (!departmentIdSet.contains(departmentEntity.getId())) {
                    departmentIdSet.add(departmentEntity.getId());
                    departments.add(new StrID<D>(departmentEntity.getId(), departmentCopier.copyB2A(departmentEntity)));
                }
                organizationEntity = departmentEntity.getHigherLevel();
            }
        }
        return departments;
    }

    @Override
    public List<StrID<J>> getMyPositions() {
        List<StrID<J>> positions = new ArrayList<StrID<J>>();
        PE personEntity = getCurrentAccountEntity();

        for (JE position : personEntity.getPositions()) {
            positions.add(new StrID<J>(position.getId(), positionCopier.copyB2A(position)));
        }

        return positions;
    }

    @Override
    public Set<String> getMyRoles() {
        return AbstractOrganizationAccountFactory.getAllRoles(getCurrentAccountEntity());
    }

    @Override
    public void updatePassword(String password, String authCode) {
        AccountsCommon.updatePassword(getCurrentAccountEntity(), password, authCode, personAccountRepo);
//        super.updatePassword(password, authCode);
//        PE personEntity = getPersonEntityWithAuthCheck(authCode);
//        personEntity.setPassword(AccountsCommon.getEncodedPassword(password));
//        putLoggingData("changePwd", "");
//        personAccountRepo.save(personEntity);
    }

//    protected PE getPersonEntityWithAuthCheck(String authCode) {
//        PE personEntity = getCurrentAccountEntity();
//        IF.isNull(personEntity.getAuthCodeKeyActiveTime(), OrganizationErrorCodes.ACCOUNT_INACTIVATED);
//        IF.not(TOTPAuthenticator.authenticate(authCode, personEntity.getAuthCodeKey()), OrganizationErrorCodes.AUTHORIZE_FAILED);
//        return personEntity;
//    }

    @Override
    public void updateCellPhone(String cellPhone, String authCode) {
        PE personEntity = checkAuthCode(authCode, getCurrentAccountEntity());
        if (!Common.isSameStr(cellPhone, personEntity.getCellphone())) {
            if (cellPhone != null) {
                IF.is(personAccountRepo.countByCellphoneAndTenant(cellPhone, getTenant()) != 0, OrganizationErrorCodes.CELL_PHONE_EXISTS);
            }
            personEntity.setCellphone(cellPhone);
            putLoggingData("cellPhone", cellPhone);
            personAccountRepo.save(personEntity);
        }
    }

    @Override
    public void updateEmail(String email, String authCode) {
        PE personEntity = checkAuthCode(authCode, getCurrentAccountEntity());
        if (!Common.isSameStr(email, personEntity.getEmail())) {
            if (email != null) {
                IF.is(personAccountRepo.countByEmailAndTenant(email, getTenant()) != 0, OrganizationErrorCodes.EMAIL_EXISTS);
            }
            personEntity.setEmail(email);
            putLoggingData("email", email);
            personAccountRepo.save(personEntity);
        }
    }

    @Override
    public String authenticatorDesc(String authCode) {
//        return super.authenticatorDesc(authCode);
        return getAuthenticatorDesc(getCurrentAccountEntity(), authCode);
//        PE personEntity = getCurrentAccountEntity();
//        if (personEntity.getAuthCodeKey() != null && personEntity.getAuthCodeKeyActiveTime() != null) {
//            IF.not(TOTPAuthenticator.authenticate(authCode, personEntity.getAuthCodeKey()), OrganizationErrorCodes.AUTHORIZE_FAILED);
//        }
//        String authKey = TOTPAuthenticator.generateAuthKey();
//        token.setAttribute("accounts.temp.authKey", authKey);
//        token.setAttribute("accounts.temp.authKey.validation",
//                Long.valueOf(System.currentTimeMillis() + 10 * 60 * 1000l));
//        return TOTPAuthenticator.build(authKey, AccountsCommon.getApplicationName(), personEntity.getName());
    }

    @Override
    public void bindAuthKey(String authCode) {
//        super.bindAuthKey(authCode);
        AccountsCommon.bindAuthKey(getCurrentAccountEntity(), authCode, personAccountRepo);
//        Long validation = token.getAttribute("accounts.temp.authKey.validation");
//        IF.is(System.currentTimeMillis() > validation, OrganizationErrorCodes.AUTH_KEY_FAILURE);
//        String authKey = token.getAttribute("accounts.temp.authKey");
//        token.removeAttribute("accounts.temp.authKey");
//        IF.not(TOTPAuthenticator.authenticate(authCode, authKey), OrganizationErrorCodes.AUTHORIZE_FAILED);
//
//        PE personEntity = getCurrentAccountEntity();
//        personEntity.setAuthCodeKey(authKey);
//        personEntity.setAuthCodeKeyActiveTime(Calendar.getInstance());
//
//        putLoggingData("bind", authKey);
//
//        personAccountRepo.save(personEntity);
//        token.setAccountCredible(true);
    }
}
