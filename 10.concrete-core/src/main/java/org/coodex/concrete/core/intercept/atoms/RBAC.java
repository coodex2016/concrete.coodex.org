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

package org.coodex.concrete.core.intercept.atoms;

//import org.aopalliance.intercept.MethodInterceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.Domain;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import java.util.HashSet;
import java.util.Set;

//import org.aspectj.lang.JoinPoint;
//import AbstractInterceptor;

/**
 * RBAC切面处理逻辑
 * Created by davidoff shen on 2016-09-05.
 */
public abstract class RBAC /*extends AbstractInterceptor*/ {

    private static Token token = TokenWrapper.getInstance();

    public static void before(RuntimeContext context, MethodInvocation joinPoint) {

        if (context.getDeclaringMethod() != null) {
            // 找profile
            Profile profile = Profile.getProfile(context.getModuleName() + ".properties");
            String[] acl = profile.getStrList(context.getMethodName());
            String domain = profile.getString("domain");

            if (domain == null) {
                Domain owner = context.getDeclaringAnnotation(Domain.class);
                if (owner != null && !Common.isBlank(owner.value().trim()))
                    domain = owner.value();
            }


            // 找AccessAllow
            if (acl == null) {
                AccessAllow accessAllow = context.getDeclaringMethod().getAnnotation(AccessAllow.class);
                if (accessAllow != null) {
                    acl = accessAllow.roles();
                    if (acl.length == 0) {
                        acl = new String[]{AccessAllow.EVERYBODY};
                    }
//                    else if (domain != null) {
//                        for (int i = 0; i < acl.length; i++) {
//                            if (!Common.isBlank(acl[i])) {
//                                acl[i] = domain + "." + acl[i];
//                            }
//                        }
//                    }

                }
            }

            rbac(acl, domain);
        }
    }

    /**
     * 基于角色验证当前账户是否有授权
     *
     * @param acl
     */
    @SuppressWarnings("unchecked")
    public static void rbac(String[] acl, String domain) {
        if (acl != null) {//需要判定权限
            Account currentAccount = getCurrentAccount();//token.currentAccount();
            //用户未登录
            Assert.isNull(currentAccount, ErrorCodes.NONE_ACCOUNT, token);
            //用户已失效
            Assert.not(currentAccount.isValid(), ErrorCodes.ACCOUNT_INVALIDATE);
            //用户不可信
            Assert.not(token.isAccountCredible(), ErrorCodes.UNTRUSTED_ACCOUNT);

            //从用户角色中过滤出匹配domain的角色
            Set<String> accountDomainRoles = getAccountDomainRoles(domain, currentAccount);

            // 特权用户，放行
            if (accountDomainRoles.contains(AccessAllow.PREROGATIVE)) return;

            Set<String> roles = Common.arrayToSet(acl);
            Assert.is(accountDomainRoles.size() == 0 ||
                    Common.intersection(roles, accountDomainRoles).size() == 0, ErrorCodes.NO_AUTHORIZATION);
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> getAccountDomainRoles(String domain, Account currentAccount) {

        Set<String> accountDomainRoles = new HashSet<String>();
        Set<String> accountRoles = currentAccount.getRoles();
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

    @SuppressWarnings("unchecked")
    private static Account getCurrentAccount() {
        return token.currentAccount();
//        AccountFactory accountFactory = BeanProviderFacade.getBeanProvider().getBean(AccountFactory.class);
//        return (accountFactory == null) ? null :
//                accountFactory.getAccountByID(token.getAttribute(ConcreteHelper.TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID));
    }
}
