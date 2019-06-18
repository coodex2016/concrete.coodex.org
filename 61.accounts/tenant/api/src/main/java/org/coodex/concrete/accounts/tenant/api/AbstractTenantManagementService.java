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

package org.coodex.concrete.accounts.tenant.api;

import org.coodex.concrete.accounts.tenant.pojo.Tenant;
import org.coodex.concrete.accounts.tenant.pojo.TenantQuery;
import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.Safely;
import org.coodex.concrete.api.pojo.PageRequest;
import org.coodex.concrete.api.pojo.PageResult;
import org.coodex.concrete.api.pojo.StrID;

import static org.coodex.concrete.accounts.AccountManagementRoles.SYSTEM_MANAGER;

/**
 * 租户管理服务，放置于租户管理端。
 * 系统管理员可以进行租户信息维护
 * <p>
 * Created by davidoff shen on 2017-05-25.
 */
@MicroService(value = "tenants", abstractive = true)
@AccessAllow(roles = {SYSTEM_MANAGER})
@Safely
public interface AbstractTenantManagementService<T extends Tenant> {

    @Description(name = "新建租户", description = "LOGGING: new 新建的租户实体信息")
    StrID<T> save(String tenant, T tenantInfo);

    @Description(name = "修改租户信息",
            description = "LOGGING: old 变更前的租户实体；new 变更后的租户实体")
    void update(String tenant, T tenantInfo);

//    @Description(name = "分页显示所有租户", description = "管理租户用，如需其他条件查询自行扩展")
//    @AccessAllow(roles = {SYSTEM_MANAGER})
//    @Safely
//    PageResult<StrID<T>> list(SortedPageRequest<T> request);

    PageResult<T> list(PageRequest<TenantQuery> request);

    @Description(name = "删除租户",
            description = "初始状态或有效期超期一定年限以上方可删除，" +
                    "LOGGING: deleted 所有被删除的实体信息")
    void delete(String tenant);

    @Description(name = "租户延续有效期")
    @MicroService("{tenant}/goDownTo")
    void goDownTo(String tenant,
                  @Description(name = "数量") int count,
                  @Description(name = "单位，0:天； 1:月; 2:季; 3:年; 其他视为天") int unit);

    @Description(name = "暂停使用", description = "需要计算余量")
    @MicroService("{tenant}/layUp")
    void layUp(String tenant);

    @Description(name = "恢复使用", description = "在恢复的时间基础上增加上余量")
    @MicroService("{tenant}/desterilize")
    void desterilize(String tenant);

    @MicroService("{tenant}/password")
    @Description(name = "重置指定租户管理员登录密码")
    void resetPassword(String tenant);

    @MicroService("{tenant}/authCode")
    @Description(name = "重置指定租户管理员的认证码")
    void resetAuthCode(String tenant);

}
