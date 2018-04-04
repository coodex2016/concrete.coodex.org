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
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.api.*;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.util.Parameter;

import java.util.List;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-05-03.
 */
@MicroService("mine")
@Abstract
@AccessAllow
public interface AbstractSelfManagementService<
        I extends Institution,
        D extends Department,
        J extends Position,
        P extends Person> extends ConcreteService {

    @MicroService("institutions")
    @Description(name = "当前用户所在单位", description = "TODO：界定排序原则")
    List<StrID<I>> getMyInstitutions();

    @MicroService("departments")
    @Description(name = "当前用户所在部门", description = "TODO: 界定排序原则")
    List<StrID<D>> getMyDepartments();

    @MicroService("positions")
    @Description(name = "当前用户的全部职位")
    List<StrID<J>> getMyPositions();


    @MicroService("roles")
    @Description(name = "当前用户的全部角色，含职位角色")
    Set<String> getMyRoles();


    @MicroService("pwd")
    @Description(name = "修改当前人员密码")
    @Safely
    void updatePassword(
            @Parameter("password") @BigString String password,
            @Parameter("authCode") @BigString String authCode);

    @MicroService("cellphone")
    @Description(name = "修改当前人员手机号")
    @Safely
    void updateCellPhone(
            @Parameter("cellPhone") @BigString String cellPhone,
            @Parameter("authCode") @BigString String authCode);

    @MicroService("email")
    @Description(name = "修改当前人员电子邮件地址")
    @Safely
    void updateEmail(
            @Parameter("email") @BigString String email,
            @Parameter("authCode") @BigString String authCode);

    @MicroService("totp")
    @Description(name = "获取待绑定的Authenticator信息",
            description = "如果原authKey已失效，authCode可以为空。新的key仅在10分钟以内有效")
    String authenticatorDesc(
            @Parameter("authCode") @BigString String authCode);

    @MicroService("auth")
    @Description(name = "绑定Authenticator")
    void bindAuthKey(
            @Parameter("authCode") @BigString String authCode);
}
