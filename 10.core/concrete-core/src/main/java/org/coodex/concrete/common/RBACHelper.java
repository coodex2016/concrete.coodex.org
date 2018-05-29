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

package org.coodex.concrete.common;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Common;

import java.util.HashSet;
import java.util.Set;

public class RBACHelper {

    public static void rbac(String[] acl) {
        rbac(acl, true);
    }

    public static void rbac(String[] acl, boolean safely) {
        rbac(acl, null, safely);
    }

    public static void rbac(String[] acl, String domain, boolean safely) {
        rbac(getCurrentAccount(), acl, domain, safely);
    }

    public static void rbac(Account account, String[] acl) {
        rbac(account, acl, true);
    }

    public static void rbac(Account account, String[] acl, boolean safely) {
        rbac(account, acl, null, safely);
    }

    public static void rbac(Account account, String[] acl, String domain, boolean safely) {
        if (acl != null) {
            Account currentAccount = getCurrentAccount();
            Token token = TokenWrapper.getInstance();
            //用户未登录
            IF.isNull(currentAccount, ErrorCodes.NONE_ACCOUNT, token);
            //用户已失效
            IF.not(currentAccount.isValid(), ErrorCodes.ACCOUNT_INVALIDATE);
            if (safely) {
                //用户不可信
                IF.not(token.isAccountCredible(), ErrorCodes.UNTRUSTED_ACCOUNT);
            }
            Set<String> mathings = matching(account, acl, domain);
            IF.is(mathings.size() == 0, ErrorCodes.NO_AUTHORIZATION);
        }
    }

    public static Set<String> matching(String[] roles, String domain) {
        return matching(getCurrentAccount(), roles, domain);
    }


    public static Set<String> matching(Account account, String[] roles, String domain) {
        if (roles == null) throw new NullPointerException("roles MUST be NOT NULL.");

        Set<String> accountDomainRoles = getAccountDomainRoles(domain, account);

        Set<String> toCheck = Common.arrayToSet(roles);
        // 特权用户，所有权限都有
        if (accountDomainRoles.contains(AccessAllow.PREROGATIVE)) return toCheck;

        return Common.intersection(toCheck, accountDomainRoles);
    }


    private static Account getCurrentAccount() {
        return TokenWrapper.getInstance().currentAccount();
    }

    private static Set<String> getAccountDomainRoles(String domain, Account account) {
        if (account == null) throw new RuntimeException("account MUST be NOT NULL.");

        Set<String> accountDomainRoles = new HashSet<String>();
        @SuppressWarnings("unchecked")
        Set<String> accountRoles = account.getRoles();
        if (accountRoles != null) {
            if (Common.isBlank(domain)) {
                accountDomainRoles.addAll(accountRoles);
            } else {
                int domainPrefixLen = domain.length() + 1;
                for (String role : accountRoles) {
                    if (Common.isBlank(role)) continue;
                    if (role.equals(AccessAllow.PREROGATIVE)) {
                        accountDomainRoles.add(AccessAllow.PREROGATIVE);
                        break;
                    }

                    if (role.startsWith(AccessAllow.PREROGATIVE + ".") && role.length() > 2) {
                        accountDomainRoles.add(role.substring(2));
                    } else if (role.startsWith(domain + ".") && role.length() > domainPrefixLen) {
                        accountDomainRoles.add(role.substring(domainPrefixLen));
                    }
                }
            }
        }
        if (!Common.isBlank(domain)) {
            if (accountDomainRoles.size() > 0) {
                // 领域非空，并且用户至少有该领域下一个角色，才能说明其可以使用该领域下的EVERYBODY
                accountDomainRoles.add(AccessAllow.EVERYBODY);
            }
        } else {//未定义领域，则是个人就行
            accountDomainRoles.add(AccessAllow.EVERYBODY);
        }
        return accountDomainRoles;
    }
}
