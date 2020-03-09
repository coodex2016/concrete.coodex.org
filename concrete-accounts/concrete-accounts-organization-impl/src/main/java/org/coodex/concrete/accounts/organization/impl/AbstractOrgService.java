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

import org.coodex.concrete.accounts.TenantAccount;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.coodex.concrete.accounts.organization.repositories.AbstractPersonAccountRepo;
import org.coodex.concrete.accounts.organization.repositories.OrganizationRepo;
import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

import static org.coodex.concrete.accounts.AccountManagementRoles.*;
import static org.coodex.concrete.accounts.AccountsCommon.getTenant;
import static org.coodex.concrete.common.OrganizationErrorCodes.NONE_THIS_ORGANIZATION;

/**
 * Created by davidoff shen on 2017-05-18.
 */
public abstract class AbstractOrgService<J extends AbstractPositionEntity, P extends AbstractPersonAccountEntity<J>> {

    private final static Logger log = LoggerFactory.getLogger(AbstractOrgService.class);


    protected Token token = TokenWrapper.getInstance();
    @Inject
    protected OrganizationRepo organizationRepo;
    @Inject
    protected AbstractPersonAccountRepo<P> personAccountRepo;


    /**
     * 检查是否具有管理orgId的权限
     * 优先级：
     * [管理所有组织]
     * AccessAllow.PREROGATIVE
     * SYSTEM_MANAGER
     * [租户管理员，需要验证所操作的组织是否为租户的组织，新建顶级单位时，需要确定租户的appSet是否和当前appSet相同]
     * TENANT_MANAGER
     * [指定组织，根据职位的的权限进行判定，当人员的职位上有ORGANIZATION_MANAGER角色时，则人员拥有职位所属组织及其下级的管理权]
     * ORGANIZATION_MANAGER
     *
     * @param orgId
     */
    protected void checkManagementPermission(String orgId) {
        ConcreteException exception = new ConcreteException(ErrorCodes.NO_AUTHORIZATION);
        Account<ClassifiableAccountID> account = token.currentAccount();
        Set<String> roles = account.getRoles();
        if (roles.contains(AccessAllow.PREROGATIVE)) return;
        if (roles.contains(SYSTEM_MANAGER)) return;
        if (roles.contains(TENANT_MANAGER)) {
            if (!Common.isBlank(orgId)) {
                OrganizationEntity organizationEntity = organizationRepo.findById(orgId).orElse(null);
                if (Common.isSameStr(organizationEntity == null ? null : organizationEntity.getTenant(), getTenant())) return;
            } else {
                if (account instanceof TenantAccount) {
                    TenantAccount tenantAccount = (TenantAccount) account;
                    if (Common.isSameStr(tenantAccount.getAppSet(), ConcreteHelper.getAppSet())) {
                        return;
                    } else {
                        log.info("{}(set {}) cannot use in this set: {}.",
                                tenantAccount.getName(),
                                tenantAccount.getAppSet(),
                                ConcreteHelper.getAppSet());
                    }
                }
            }
        }

        if (!Common.isBlank(orgId)) {
            P person = personAccountRepo.findById(account.getId().getId()).orElse(null);
            if(person != null) {
                for (J position : person.getPositions()) {
                    if (position.getRoles() != null && position.getRoles().contains(ORGANIZATION_MANAGER)) {
                        // orgId是否是position.belongTo的下级机构
                        String positionBelongTo = position.getBelongTo().getId();
                        OrganizationEntity organizationEntity = organizationRepo.findById(orgId).orElse(null);
                        while (organizationEntity != null) {
                            if (positionBelongTo.equals(organizationEntity.getId()))
                                return;
                            organizationEntity = organizationEntity.getHigherLevel();
                        }
                    }
                }
            }
        }
        throw exception;
    }

    /**
     * 循环检查，递归线性上级是否存在自己的id
     *
     * @param higherLevelEntity
     * @param id
     */
    protected void circleCheck(OrganizationEntity higherLevelEntity, String id) {
        OrganizationEntity organizationEntity = higherLevelEntity;
        while (organizationEntity != null) {
            IF.is(id.equals(organizationEntity.getId()), OrganizationErrorCodes.HIGHER_LEVEL_CIRCULATION);
            organizationEntity = organizationEntity.getHigherLevel();
        }
    }

    /**
     * 检查同一组织下是否存在重名的组织
     *
     * @param higherLevel
     * @param name
     * @param id
     */
    protected void checkDuplication(String higherLevel, String name, String id) {
        if (id == null) {
            IF.notNull(organizationRepo.findOneByTenantAndNameAndHigherLevelId(
                    getTenant(), name, higherLevel), OrganizationErrorCodes.DUPLICATED_NAME);
        } else {
            IF.notNull(organizationRepo.findOneByTenantAndNameAndHigherLevelIdAndIdNot(
                    getTenant(), name, higherLevel, id), OrganizationErrorCodes.DUPLICATED_NAME);
        }
    }

    /**
     * 归属组织是否存在，部门管理服务使用
     *
     * @param belongTo
     */
    protected OrganizationEntity checkBelongToExists(String belongTo) {
        IF.isNull(belongTo, NONE_THIS_ORGANIZATION);
        return IF.isNull(organizationRepo.findById(belongTo).orElse(null), NONE_THIS_ORGANIZATION);
    }
}
