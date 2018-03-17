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

import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.SaaSAccount;
import org.coodex.concrete.common.TenantBuilder;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;

import java.io.Serializable;

/**
 * Created by davidoff shen on 2017-05-25.
 */
public class SaaSTenantBuilder implements TenantBuilder {

    private Token token = TokenWrapper.getInstance();

    @Override
    public String getTenant() {
        Account<? extends Serializable> account = token.currentAccount();
        if (account != null && account instanceof SaaSAccount) {
            return ((SaaSAccount) account).getTenant();
        }
        return null;
    }
}
