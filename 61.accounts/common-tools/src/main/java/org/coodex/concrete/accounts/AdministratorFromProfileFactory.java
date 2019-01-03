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

package org.coodex.concrete.accounts;

import org.coodex.config.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * 在administrator.properties中定义管理员信息，关键项有：
 * <ul>
 * <li>encoded.password: 系统所使用的PasswordGenerator编码方式的密码</li>
 * <li>authKey: TOTP的key，以Base32编码的5的倍数字节的内容，推荐20字节</li>
 * <li>name: 管理员名称，默认Administrator</li>
 * <li>roles: 管理员角色，以逗号分隔的角色名。默认SystemManager</li>
 * <li>valid: 是否启用，默认true。当系统初始化管理完成后，建议禁用Administrator账户</li>
 * </ul>
 * Created by davidoff shen on 2017-05-19.
 */
public class AdministratorFromProfileFactory extends AbstractAdministratorFactory {

    public static final String TAG_ADMIN = "administrator";

//    public static final Profile_Deprecated ADMINISTRATOR_INFO = Profile_Deprecated.getProfile("administrator.properties");

    @Override
    protected Administrator getAdministrator(String id) {
        return new AdministratorFromProfile(id);
    }

//    private static final Administrator ADMINISTRATOR = new Administrator() {
//
//        @Override
//        public boolean verify(String password, String authCode) {
//            if (password == null || !password.equals(ADMINISTRATOR_INFO.getString("encoded.password",
//                    AccountsCommon.getDefaultPassword()))) return false;
//
//            return TOTPAuthenticator.authenticate(authCode, ADMINISTRATOR_INFO.getString("authKey"));
//        }
//
//        private AccountIDImpl id = new AccountIDImpl(AccountIDImpl.TYPE_ADMINISTRATOR, Common.getUUIDStr());
//
//        @Override
//        public String getName() {
//            return ADMINISTRATOR_INFO.getString("name", "administrator");
//        }
//
//        @Override
//        public AccountIDImpl getId() {
//            return id;
//        }
//
//        @Override
//        public Set<String> getRoles() {
//            return new HashSet<String>(Arrays.asList(
//                    ADMINISTRATOR_INFO.getStrList("roles", ",",
//                            new String[]{AccountManagementRoles.SYSTEM_MANAGER})
//            ));
//        }
//
//        @Override
//        public boolean isValid() {
//            return ADMINISTRATOR_INFO.getBool("valid", true);
//        }
//    };

    @Override
    protected Administrator getAdministrator(String id, String tenant) {
        return new AdministratorFromProfile(id, tenant);
    }

    private static class AdministratorFromProfile implements Administrator {
        private String uuid;
        private String tenant;

        public AdministratorFromProfile(String uuid) {
            this.uuid = uuid;
        }

        public AdministratorFromProfile(String uuid, String tenant) {
            this.uuid = uuid;
            this.tenant = tenant;
        }

        @Override
        public boolean verify(String password, String authCode) {
            if (password == null || !password.equals(Config.getValue("encoded.password",
                    AccountsCommon.getDefaultPassword(), TAG_ADMIN, getAppSet()))) return false;

            return TOTPAuthenticator.authenticate(authCode, Config.get("authKey", TAG_ADMIN, getAppSet()));
        }

//        private AccountIDImpl id = ;

        @Override
        public String getName() {
            return Config.getValue("name", "administrator", TAG_ADMIN, getAppSet());
        }

        @Override
        public AccountIDImpl getId() {
            return new AccountIDImpl(AccountIDImpl.TYPE_ADMINISTRATOR, uuid);
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<String>(Arrays.asList(
                    Config.getArray("roles", ",",
                            new String[]{AccountManagementRoles.SYSTEM_MANAGER},
                            TAG_ADMIN, getAppSet())
            ));
        }

        @Override
        public boolean isValid() {
            return Config.getValue("valid", true, TAG_ADMIN, getAppSet());
        }

        @Override
        public String getTenant() {
            return tenant;
        }
    }
}
