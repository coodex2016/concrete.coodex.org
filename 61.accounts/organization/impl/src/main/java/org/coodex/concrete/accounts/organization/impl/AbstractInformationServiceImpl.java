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
import org.coodex.concrete.accounts.organization.api.AbstractInformationService;
import org.coodex.concrete.accounts.organization.entities.*;
import org.coodex.concrete.accounts.organization.pojo.*;
import org.coodex.concrete.accounts.organization.pojo.full.DepartmentFull;
import org.coodex.concrete.accounts.organization.pojo.full.InstitutionFull;
import org.coodex.concrete.accounts.organization.repositories.AbstractDepartmentRepo;
import org.coodex.concrete.accounts.organization.repositories.AbstractInstitutionRepo;
import org.coodex.concrete.accounts.organization.repositories.AbstractPositionRepo;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.OrganizationErrorCodes;
import org.coodex.concrete.common.TwoWayCopier;
import org.springframework.data.domain.Sort;

import javax.inject.Inject;
import java.util.*;

import static org.coodex.concrete.accounts.AccountsCommon.getTenant;

/**
 * Created by davidoff shen on 2017-05-18.
 */
public abstract class AbstractInformationServiceImpl<
        I extends Institution, D extends Department,
        J extends Position, P extends Person,
        IE extends AbstractInstitutionEntity,
        DE extends AbstractDepartmentEntity,
        JE extends AbstractPositionEntity,
        PE extends AbstractPersonAccountEntity<JE>>
        extends AbstractOrgService<JE, PE>
        implements AbstractInformationService<I, D, J, P> {


    @Inject
    protected AbstractInstitutionRepo<IE> institutionRepo;
    @Inject
    protected TwoWayCopier<I, IE> institutionCopier;

    @Inject
    protected AbstractDepartmentRepo<DE> departmentRepo;
    @Inject
    protected TwoWayCopier<D, DE> departmentCopier;

    @Inject
    protected AbstractPositionRepo<JE> positionRepo;
    @Inject
    protected TwoWayCopier<J, JE> positionCopier;

    @Inject
    protected TwoWayCopier<P, PE> personCopier;


    @Override
    public List<InstitutionFull<I, D, J, P>> get() {

        List<InstitutionFull<I, D, J, P>> institutionFullList = new ArrayList<InstitutionFull<I, D, J, P>>();
        for (IE institution : institutionRepo.findByTenantAndHigherLevelIdIsNullOrderByDisplayOrderDesc(getTenant())) {
            institutionFullList.add($getOneInstitutionFull(institution.getId()));
        }
        return institutionFullList;
    }

    protected InstitutionFull<I, D, J, P> $getOneInstitutionFull(String id) {

        // TODO 需要考虑缓存
        IE institutionEntity = IF.isNull(institutionRepo.findOne(id), OrganizationErrorCodes.NONE_THIS_INSTITUTION);
        InstitutionFull<I, D, J, P> institutionFull = new InstitutionFull<I, D, J, P>();
        institutionFull.setId(institutionEntity.getId());
        institutionFull.setInstitution(institutionCopier.copyB2A(institutionEntity));

        // 添加下级单位
        institutionFull.setInstitutions(new ArrayList<InstitutionFull<I, D, J, P>>());
        for (IE subInstitution : institutionRepo.findByTenantAndHigherLevelIdOrderByDisplayOrderDesc(
                getTenant(), institutionEntity.getId())) {
            institutionFull.getInstitutions().add($getOneInstitutionFull(subInstitution.getId()));
        }

        // 添加下级部门
        institutionFull.setDepartments(new ArrayList<DepartmentFull<D, J, P>>());
        for (DE department : departmentRepo.findByTenantAndHigherLevelIdOrderByDisplayOrderDesc(
                getTenant(), institutionEntity.getId())) {
            institutionFull.getDepartments().add($getOneDepartmentFull(department.getId()));
        }


        // 添加职位、人员
        institutionFull.setPersons(new ArrayList<StrID<P>>());
        institutionFull.setPositions(new ArrayList<StrID<J>>());
        appendPositionsAndPersons(institutionEntity, institutionFull.getPositions(), institutionFull.getPersons());
        return institutionFull;
    }

    protected void appendPositionsAndPersons(OrganizationEntity institutionEntity, List<StrID<J>> positionList, List<StrID<P>> personList) {
        Set<String> personIdSet = new HashSet<String>();
        for (JE positionEntity : positionRepo.findByBelongOrderByDisplayOrderDesc(institutionEntity.getId())) {
            positionList.add(new StrID<J>(positionEntity.getId(), positionCopier.copyB2A(positionEntity)));
            for (PE personEntity : personAccountRepo.findAll(
                    SpecCommon.<PE, JE>memberOf("positions", positionEntity),
                    new Sort(new Sort.Order(Sort.Direction.DESC, "displayOrder")))) {

                if (!personIdSet.contains(personEntity.getId())) {
                    personIdSet.add(personEntity.getId());
                    personList.add(new StrID<P>(personEntity.getId(), personCopier.copyB2A(personEntity)));
                }
            }
        }
    }

    @Override
    public InstitutionFull<I, D, J, P> getOneInstitutionFull(String id) {
        return $getOneInstitutionFull(id);
    }

    protected DepartmentFull<D, J, P> $getOneDepartmentFull(String id) {
        // TODO 需要考虑缓存
        DE departmentEntity = IF.isNull(departmentRepo.findOne(id), OrganizationErrorCodes.NONE_THIS_DEPARTMENT);
        DepartmentFull<D, J, P> departmentFull = new DepartmentFull<D, J, P>();
        departmentFull.setDepartment(departmentCopier.copyB2A(departmentEntity));
        departmentFull.setId(departmentEntity.getId());
        departmentFull.setPersons(new ArrayList<StrID<P>>());
        departmentFull.setPositions(new ArrayList<StrID<J>>());
        appendPositionsAndPersons(departmentEntity, departmentFull.getPositions(), departmentFull.getPersons());
        return departmentFull;
    }

    @Override
    public DepartmentFull<D, J, P> getOneDepartmentFull(String id) {
        return $getOneDepartmentFull(id);
    }

    @Override
    public List<StrID<Organization>> getHigherLevelOrganizations(String id) {
        OrganizationEntity organizationEntity =
                IF.isNull(organizationRepo.findOne(id), OrganizationErrorCodes.NONE_THIS_ORGANIZATION).getHigherLevel();

        List<StrID<Organization>> organizationList = new ArrayList<StrID<Organization>>();
        while (organizationEntity != null) {
            organizationList.add(new StrID<Organization>(
                    organizationEntity.getId(),
                    organizationEntity instanceof AbstractDepartmentEntity ?
                            departmentCopier.copyB2A((DE) organizationEntity) :
                            institutionCopier.copyB2A((IE) organizationEntity)));
        }

        // 反序
        StrID<Organization>[] array = organizationList.toArray(new StrID[0]);
        for (int i = 0, l = array.length, h = l / 2; i < h; i++) {
            StrID<Organization> temp = array[i];
            array[i] = array[l - 1 - i];
            array[l - 1 - i] = temp;
        }

        return Arrays.asList(array);
    }


    @Override
    public StrID<I> getInstitution(String id) {
        IE institutionEntity = IF.isNull(institutionRepo.findOne(id), OrganizationErrorCodes.NONE_THIS_INSTITUTION);
        return new StrID<I>(institutionEntity.getId(), institutionCopier.copyB2A(institutionEntity));
    }

    @Override
    public List<StrID<I>> getInstitutions() {
        List<StrID<I>> list = new ArrayList<StrID<I>>();
        for (IE institutionEntity : institutionRepo.findByTenantAndHigherLevelIdIsNullOrderByDisplayOrderDesc(getTenant())) {
            list.add(new StrID<I>(institutionEntity.getId(), institutionCopier.copyB2A(institutionEntity)));
        }
        return list;
    }

    @Override
    public List<StrID<I>> getInstitutionsOf(String higherLevel) {
        if (higherLevel != null)
            checkBelongToExists(higherLevel);
        List<StrID<I>> list = new ArrayList<StrID<I>>();
        for (IE institutionEntity : institutionRepo.findByTenantAndHigherLevelIdOrderByDisplayOrderDesc(getTenant(), higherLevel)) {
            list.add(new StrID<I>(institutionEntity.getId(), institutionCopier.copyB2A(institutionEntity)));
        }
        return list;
    }

    @Override
    @Deprecated
    public List<StrID<D>> getDepartmentsOfInstitution(String institution) {
        IF.isNull(institutionRepo.findOne(institution), OrganizationErrorCodes.NONE_THIS_INSTITUTION);
        return $getDepartmentsOfOrganization(institution);
    }

    @Override
    @Deprecated
    public List<StrID<J>> getPositionsOfInstitution(String institution) {
        IF.isNull(institutionRepo.findOne(institution), OrganizationErrorCodes.NONE_THIS_INSTITUTION);
        return $getPositionsOfOrganization(institution);
    }

    @Override
    @Deprecated
    public List<StrID<P>> getPersonsOfInstitution(String institution) {
        IF.isNull(institutionRepo.findOne(institution), OrganizationErrorCodes.NONE_THIS_INSTITUTION);
        return $getPersonsOfOrganization(institution);
    }

    @Override
    @Deprecated
    public List<StrID<D>> getDepartmentsOfDepartment(String department) {
        IF.isNull(departmentRepo.findOne(department), OrganizationErrorCodes.NONE_THIS_DEPARTMENT);
        return $getDepartmentsOfOrganization(department);
    }

    @Override
    @Deprecated
    public List<StrID<J>> getPositionsOfDepartment(String department) {
        IF.isNull(departmentRepo.findOne(department), OrganizationErrorCodes.NONE_THIS_DEPARTMENT);
        return $getPositionsOfOrganization(department);
    }

    @Override
    @Deprecated
    public List<StrID<P>> getPersonsOfDepartment(String department) {
        IF.isNull(departmentRepo.findOne(department), OrganizationErrorCodes.NONE_THIS_DEPARTMENT);
        return $getPersonsOfOrganization(department);
    }


    protected List<StrID<D>> $getDepartmentsOfOrganization(String organization) {
        checkBelongToExists(organization);
        List<StrID<D>> list = new ArrayList<StrID<D>>();
        for (DE departmentEntity : departmentRepo.findByTenantAndHigherLevelIdOrderByDisplayOrderDesc(
                getTenant(), organization)) {
            list.add(new StrID<D>(departmentEntity.getId(), departmentCopier.copyB2A(departmentEntity)));
        }
        return list;
    }

    @Override
    public List<StrID<D>> getDepartmentsOfOrganization(String organization) {
        return $getDepartmentsOfOrganization(organization);
    }

    protected List<StrID<J>> $getPositionsOfOrganization(String organization) {
        checkBelongToExists(organization);
        List<StrID<J>> list = new ArrayList<StrID<J>>();
        for (JE positionEntity : positionRepo.findByBelongOrderByDisplayOrderDesc(organization)) {
            list.add(new StrID<J>(positionEntity.getId(), positionCopier.copyB2A(positionEntity)));
        }
        return list;
    }

    @Override
    public List<StrID<J>> getPositionsOfOrganization(String organization) {
        return $getPositionsOfOrganization(organization);
    }

    protected List<StrID<P>> $getPersonsOfOrganization(String organization) {
        List<StrID<P>> personList = new ArrayList<StrID<P>>();
        appendPositionsAndPersons(checkBelongToExists(organization), new ArrayList<StrID<J>>(), personList);
        return personList;
    }

    @Override
    public List<StrID<P>> getPersonsOfOrganization(String organization) {
        return $getPersonsOfOrganization(organization);
    }

    @Override
    public List<StrID<I>> getInstitutionsOfPerson(String person) {
        PE personEntity = IF.isNull(personAccountRepo.findOne(person), OrganizationErrorCodes.PERSON_NOT_EXISTS);
        Set<String> institutions = new HashSet<String>();
        List<StrID<I>> institutionList = new ArrayList<StrID<I>>();
        for (JE positionEntity : personEntity.getPositions()) {
            OrganizationEntity organizationEntity = positionEntity.getBelongTo();
            while (organizationEntity != null) {
                if (organizationEntity instanceof AbstractInstitutionEntity) {
                    if (!institutions.contains(organizationEntity.getId())) {
                        institutions.add(organizationEntity.getId());
                        institutionList.add(
                                new StrID<I>(organizationEntity.getId(),
                                        institutionCopier.copyB2A((IE) organizationEntity)));
                    }
                }
                organizationEntity = organizationEntity.getHigherLevel();
            }
        }
        return institutionList;
    }

    @Override
    public List<StrID<D>> getDepartmentsOfPerson(String person) {
        PE personEntity = IF.isNull(personAccountRepo.findOne(person), OrganizationErrorCodes.PERSON_NOT_EXISTS);
        Set<String> departments = new HashSet<String>();
        List<StrID<D>> departmentList = new ArrayList<StrID<D>>();
        for (JE positionEntity : personEntity.getPositions()) {
            OrganizationEntity organizationEntity = positionEntity.getBelongTo();
            while (organizationEntity != null) {
                if (organizationEntity instanceof AbstractDepartmentEntity) {
                    if (!departments.contains(organizationEntity.getId())) {
                        departments.add(organizationEntity.getId());
                        departmentList.add(
                                new StrID<D>(organizationEntity.getId(),
                                        departmentCopier.copyB2A((DE) organizationEntity)));
                    }
                } else {
                    break;
                }
                organizationEntity = organizationEntity.getHigherLevel();
            }
        }
        return departmentList;
    }

    @Override
    public List<StrID<J>> getPositionsOfPerson(String person) {
        PE personEntity = IF.isNull(personAccountRepo.findOne(person), OrganizationErrorCodes.PERSON_NOT_EXISTS);
        List<StrID<J>> positionList = new ArrayList<StrID<J>>();
        for (JE positionEntity : personEntity.getPositions()) {
            positionList.add(new StrID<J>(positionEntity.getId(), positionCopier.copyB2A(positionEntity)));
        }
        return positionList;
    }
}
