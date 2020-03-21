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

import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.OrganizationEntity;
import org.coodex.concrete.accounts.organization.repositories.AbstractPersonAccountRepo;
import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.ClassifiableAccountFactory;
import org.coodex.concrete.common.ClassifiableAccountID;
import org.coodex.concrete.common.IF;
import org.coodex.config.Config;
import org.coodex.copier.AbstractCopier;
import org.coodex.copier.Copier;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static org.coodex.concrete.accounts.AccountConstants.TYPE_ORGANIZATION;
import static org.coodex.concrete.common.AccountsErrorCodes.NONE_THIS_ACCOUNT;

/**
 * Created by davidoff shen on 2017-05-09.
 */
public abstract class AbstractOrganizationAccountFactory
        <J extends AbstractPositionEntity,
                P extends AbstractPersonAccountEntity<J>>
        extends ClassifiableAccountFactory {

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    protected AbstractPersonAccountRepo<P> accountRepo;
    private Copier<P, OrganizationAccount> accountCopier = new AbstractCopier<P, OrganizationAccount>() {
        @Override
        public OrganizationAccount copy(P p, OrganizationAccount organizationAccount) {

            organizationAccount.setId(new ClassifiableAccountID(TYPE_ORGANIZATION, p.getId()));
            organizationAccount.setName(p.getName());
            organizationAccount.setRoles(getAllRoles(p));
            organizationAccount.setTenant(p.getTenant());
            return organizationAccount;
        }
    };

    private SingletonMap<String, OrganizationAccount> accountSingletonMap = SingletonMap.<String, OrganizationAccount>builder()
            .function(new Function<String, OrganizationAccount>() {
                @Override
                public OrganizationAccount apply(String key) {
                    P person = accountRepo.findById(key).orElse(null);
                    return person == null ? null : accountCopier.copy(person);
                }
            })
            .maxAge(Config.getValue("cache.object.life", 10L,
                    AbstractOrganizationAccountFactory.class.getPackage().getName()
            ) * 60L * 1000L)
            .build();

//    private ConcreteCache<String, OrganizationAccount> accountCache = new ConcreteCache<String, OrganizationAccount>() {
//        @Override
//        protected OrganizationAccount load(String key) {
//            P person = accountRepo.findById(key).get();
//            return person == null ? null : accountCopier.copy(person);
//        }
//
//        @Override
//        protected String getRule() {
//            return AbstractOrganizationAccountFactory.class.getPackage().getName();
//        }
//    };

    public static Set<String> getAllRoles(AbstractPersonAccountEntity<? extends AbstractPositionEntity> p) {
        Set<String> roles = new HashSet<>();
        if (p.getPositions() != null) {
            for (AbstractPositionEntity position : p.getPositions()) {
                Set<String> positionRoles = Common.join(p.getRoles(), position.getRoles());
                String domain = getDomain(position);
                boolean blankDomain = Common.isBlank(domain);
                for (String role : positionRoles) {
                    roles.add(role);
                    if (!blankDomain && !role.startsWith(domain + ".")) {
                        roles.add(domain + "." + role);
                    }
                }
                roles.addAll(Common.join(position.getRoles()));
            }
        }
        return Common.join(roles, p.getRoles());
    }

    public static String getDomain(AbstractPositionEntity position) {
        OrganizationEntity organizationEntity = position.getBelongTo();
        StringBuilder domain = new StringBuilder();
        while (organizationEntity != null) {
            String orgDomain = Common.trim(organizationEntity.getDomain(), '.');
            if (!Common.isBlank(orgDomain)) {
                if (domain.length() != 0) domain.append('.');
                domain.append(orgDomain);
            }
            organizationEntity = Common.isBlank(organizationEntity.getHigherLevelId()) ?
                    null : organizationEntity.getHigherLevel();
        }
        return domain.toString();
    }

//    @Override
//    public boolean accept(ClassifiableAccountID accountID) {
//        return accountID != null && accountID.getCategory() == TYPE_ORGANIZATION;
//    }


    @Override
    protected Integer[] getSupportTypes() {
        return new Integer[]{TYPE_ORGANIZATION};
    }

    @Override
    public Account<ClassifiableAccountID> getAccountByID(ClassifiableAccountID id) {
        return IF.isNull(accountSingletonMap.get(id.getId()), NONE_THIS_ACCOUNT);
    }
}
