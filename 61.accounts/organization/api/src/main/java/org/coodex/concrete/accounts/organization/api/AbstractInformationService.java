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

package org.coodex.concrete.accounts.organization.api;

import org.coodex.concrete.accounts.organization.pojo.*;
import org.coodex.concrete.accounts.organization.pojo.full.DepartmentFull;
import org.coodex.concrete.accounts.organization.pojo.full.InstitutionFull;
import org.coodex.concrete.api.Abstract;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.util.Parameter;

import java.util.List;

/**
 * 信息浏览服务
 * Created by davidoff shen on 2017-05-02.
 */
@Abstract
public interface AbstractInformationService<
        I extends Institution,
        D extends Department,
        J extends Position,
        P extends Person> extends ConcreteService {


    @MicroService("all")
    @Description(name = "全部组织结构信息")
    List<InstitutionFull<I, D, J, P>> get();

    @MicroService("all/institutions")
    @Description(name = "某一个单位下的全部组织结构信息，含指定单位")
    InstitutionFull<I, D, J, P> getOneInstitutionFull(
            @Parameter("id") String id);

    @MicroService("all/departments")
    @Description(name = "某一个部门下的全部组织结构信息，含指定部门")
    DepartmentFull<D, J, P> getOneDepartmentFull(
            @Parameter("id") String id);

    @MicroService("organizations/{id}/higherLevelOrganizations")
    @Description(
            name = "某一个组织的上级组织信息，不含指定部门",
            description = "排序方式，约远越靠前"
    )
    List<StrID<Organization>> getHigherLevelOrganizations(
            @Parameter("id") String id);

    @MicroService("institutions")
    @Description(name = "获取一个单位信息")
    StrID<I> getInstitution(
            @Parameter("id") String id);

    @MicroService("institutions")
    @Description(name = "获取全部顶级单位信息")
    List<StrID<I>> getInstitutions();

    @MicroService("institutions/{higherLevel}/institutions")
    @Description(name = "某单位下的全部直属单位")
    List<StrID<I>> getInstitutionsOf(
            @Parameter("higherLevel") String higherLevel);

    @MicroService("institutions/{institution}/departments")
    @Description(name = "某单位下的全部直属部门")
    @Deprecated
    List<StrID<D>> getDepartmentsOfInstitution(
            @Parameter("institution") String institution);

    @MicroService("institutions/{institution}/positions")
    @Description(name = "某单位下的职位")
    @Deprecated
    List<StrID<J>> getPositionsOfInstitution(
            @Parameter("institution") String institution);

    @MicroService("institutions/{institution}/persons")
    @Description(name = "某单位下的人员")
    @Deprecated
    List<StrID<P>> getPersonsOfInstitution(
            @Parameter("institution") String institution);

    @MicroService("departments/{department}/departments")
    @Description(name = "某部门下的全部直属部门")
    @Deprecated
    List<StrID<D>> getDepartmentsOfDepartment(
            @Parameter("department") String department);

    @MicroService("departments/{department}/positions")
    @Description(name = "某部门下的职位")
    @Deprecated
    List<StrID<J>> getPositionsOfDepartment(
            @Parameter("department") String department);

    @MicroService("departments/{department}/persons")
    @Description(name = "某部门下的人员")
    @Deprecated
    List<StrID<P>> getPersonsOfDepartment(
            @Parameter("department") String department);

    @MicroService("organizations/{organization}/departments")
    @Description(name = "某机构下的全部直属部门")
    List<StrID<D>> getDepartmentsOfOrganization(
            @Parameter("organization") String organization);

    @MicroService("organizations/{organization}/positions")
    @Description(name = "某机构下的职位")
    List<StrID<J>> getPositionsOfOrganization(
            @Parameter("organization") String organization);

    @MicroService("organizations/{organization}/persons")
    @Description(name = "某机构下的人员")
    List<StrID<P>> getPersonsOfOrganization(
            @Parameter("organization") String organization);


    @MicroService("persons/{person}/institutions")
    @Description(name = "人员所在单位", description = "TODO：界定排序原则")
    List<StrID<I>> getInstitutionsOfPerson(
            @Parameter("person") String person);

    @MicroService("persons/{person}/departments")
    @Description(name = "人员所在部门", description = "TODO: 界定排序原则")
    List<StrID<D>> getDepartmentsOfPerson(
            @Parameter("person") String person);

    @MicroService("persons/{person}/positions")
    @Description(name = "人员的全部职位")
    List<StrID<J>> getPositionsOfPerson(
            @Parameter("person") String person);


}
