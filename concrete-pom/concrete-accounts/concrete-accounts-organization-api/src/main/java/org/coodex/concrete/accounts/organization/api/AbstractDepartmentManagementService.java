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

import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.Safely;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.util.Parameter;

import static org.coodex.concrete.accounts.AccountManagementRoles.*;

/**
 * Created by davidoff shen on 2017-04-28.
 */
@ConcreteService(value = "departments", nonspecific = true)
@AccessAllow(roles = {SYSTEM_MANAGER, TENANT_MANAGER, ORGANIZATION_MANAGER})
@Safely
public interface AbstractDepartmentManagementService<D extends Department> {

    @Description(name = "新建部门", description = "LOGGING: new 新建的部门实体信息")
    StrID<D> save(
            @Parameter("department") D department,
            @Parameter("higherLevel") String higherLevel);


    @Description(name = "修改部门信息",
            description = "LOGGING: old 变更前的部门实体；new 变更后的部门实体")
    @ConcreteService("{id}")
    void update(
            @Parameter("id") String id,
            @Parameter("department") D department);

    @ConcreteService("{id}/changeTo")
    @Description(name = "变更上级",
            description = "上级可以是单位，也可以是部门。LOGGING: original 原上级组织实体；target 变更后的上级组织实体")
    void updateHigherLevel(
            @Parameter("id") String id,
            @Parameter("higherLevel") String higherLevel);

    @ConcreteService("{id}/order")
    @Description(name = "调整部门显示顺序",
            description = "LOGGING: original 原显示顺序；target 变更后的显示顺序")
    void updateOrder(
            @Parameter("id") String id,
            @Parameter("order") Integer order);

    @Description(name = "删除部门",
            description = "删除部门时，部门、下属部门应无人员，职位、下属部门均被删除。" +
                    "LOGGING: deleted 所有被删除的实体信息")
    @ConcreteService("{id}")
    void delete(
            @Parameter("id") String id);
}
