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

import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.api.*;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.util.Parameter;

import java.util.Set;

import static org.coodex.concrete.accounts.AccountManagementRoles.*;

/**
 * Created by davidoff shen on 2017-04-28.
 */
@ConcreteService(value = "positions", nonspecific = true)
@AccessAllow(roles = {SYSTEM_MANAGER, TENANT_MANAGER, ORGANIZATION_MANAGER})
@Safely
public interface AbstractPositionManagementService<P extends Position> {

    @Description(name = "新建职位")
    StrID<P> save(
            @Parameter("position") P position,
            @Parameter("belong") String belong);

    @Description(name = "修改职位信息")
    @ConcreteService("{id}")
    void update(
            @Parameter("id") String id,
            @Parameter("position") P position);

    @Description(name = "变更职位归属")
    @ConcreteService("{id}/changeTo")
    void updateBelongTo(
            @Parameter("id") String id,
            @Parameter("belong") String belong);

    @Description(name = "调整职位顺序")
    @ConcreteService("{id}/order")
    void updateOrder(
            @Parameter("id") String id,
            @Parameter("order") Integer order);

    @Description(name = "删除职位")
    @ConcreteService("{id}")
    void delete(@Parameter("id") String id);

    @ConcreteService("{id}/roles")
    @Description(name = "为职位赋角色", description = "以新角色为准")
    void grantTo(
            @Parameter("id") String id,
            @Parameter("roles") String[] roles);

    @ConcreteService("{id}/roles")
    @Description(name = "获取职位角色")
    @AccessAllow
    Set<String> roles(@Parameter("id") String id);

}
