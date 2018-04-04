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

import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.api.*;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.util.Parameter;

import java.util.Set;

import static org.coodex.concrete.accounts.AccountManagementRoles.ORGANIZATION_MANAGER;
import static org.coodex.concrete.accounts.AccountManagementRoles.SYSTEM_MANAGER;
import static org.coodex.concrete.accounts.AccountManagementRoles.TENANT_MANAGER;

/**
 * Created by davidoff shen on 2017-04-28.
 */
@MicroService("persons")
@Abstract
@AccessAllow(roles = {SYSTEM_MANAGER, TENANT_MANAGER, ORGANIZATION_MANAGER})
@Safely
public interface AbstractPersonManagementService<P extends Person> extends ConcreteService {

    @Description(name = "新建人员")
    StrID<P> save(
            @Parameter("person") P person,
            @Parameter("positions") String[] positions);

    @Description(name = "修改人员信息")
    void update(
            @Parameter("id") String id,
            @Parameter("person") P person);

    @Description(name = "变更人员职位")
    @MicroService("{id}/positions")
    void updatePositions(
            @Parameter("id") String id,
            @Parameter("positions") String[] positions);

    @Description(name = "调整人员顺序")
    @MicroService("{id}/order")
    void updateOrder(
            @Parameter("id") String id,
            @Parameter("order") Integer order);

    @Description(name = "删除人员")
    void delete(
            @Parameter("id") String id);

    @MicroService("{id}/roles")
    @Description(name = "为人员赋角色", description = "以新角色为准")
    void grantTo(
            @Parameter("id") String id,
            @Parameter("roles") String[] roles);

    @MicroService("{id}/roles")
    @Description(name = "获取人员的角色", description = "人员的直接角色，不包含人员职位的角色")
    Set<String> personRoles(
            @Parameter("id") String id);

    @MicroService("{id}/allRoles")
    @Description(name = "获取人员全部角色", description = "包含职位角色")
    Set<String> allRoles(
            @Parameter("id") String id);

    @MicroService("{id}/password")
    @Description(name = "重置指定人员登录密码")
    void resetPassword(
            @Parameter("id") String id);

    @MicroService("{id}/authCode")
    @Description(name = "重置指定人员的认证码")
    void resetAuthCode(@Parameter("id") String id);


}
