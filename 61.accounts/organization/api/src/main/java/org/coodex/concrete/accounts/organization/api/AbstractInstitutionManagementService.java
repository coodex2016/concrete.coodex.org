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

import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.api.*;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.util.Parameter;

import static org.coodex.concrete.accounts.AccountManagementRoles.*;

/**
 * Created by davidoff shen on 2017-04-28.
 */
@MicroService("institutions")
@Abstract
@AccessAllow(roles = {SYSTEM_MANAGER,TENANT_MANAGER, ORGANIZATION_MANAGER})
@Safely
public interface AbstractInstitutionManagementService<I extends Institution> extends ConcreteService {
    @Description(name = "新建单位", description = "LOGGING: new 新建单位的实体数据")
    StrID<I> save(
            @Parameter("institution") @Description(name = "单位信息") I institution,
            @Description(name = "上级单位", description = "可为空")
            @Parameter("higherLevel") @BigString String higherLevel);

    @Description(name = "更新单位信息",
            description = "LOGGING: old 单位实体变更前数据; new 变更后的实体数据")
    void update(
            @Parameter("id") String id,
            @Parameter("institution") I institution);

    @MicroService("{id}/changeTo")
    @Description(name = "变更上级单位",
            description = "LOGGING: original 原上级单位信息; target 变更之后的上级单位信息")
    void updateHigherLevel(
            @Parameter("id") String id,
            @Parameter("higherLevel") String higherLevel);

    @MicroService("{id}/order")
    @Description(name = "调整单位显示顺序",
            description = "越大越靠前，相同值则按创建时间升序排序；LOGGING: original 原顺序; target 调整后顺序。")
    void updateOrder(
            @Parameter("id") String id,
            @Parameter("order") Integer order);

    @Description(name = "删除单位",
            description = "删除单位时，单位、下属单位、部门应没有人员方可删除，下属单位、部门、职位均被删除。"
                    + "LOGGING: deleted 所有被删除的实体信息")
    @MicroService
    void delete(
            @Parameter("id") String id);
}
