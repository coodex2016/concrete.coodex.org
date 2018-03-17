/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.accounts;

/**
 * Created by davidoff shen on 2017-04-28.
 */
public class AccountManagementRoles {
    // 系统管理员
    public static final String SYSTEM_MANAGER = "SystemManager";

    // 租户管理员
    public static final String TENANT_MANAGER = "tenantManager";
    // 组织管理员
    // 组织管理员角色需要赋予职位，从而确定账户可以管理哪些部门
    // 账户可以管理角色所在的组织及其下属组织
    public static final String ORGANIZATION_MANAGER = "OrgManager";


}
