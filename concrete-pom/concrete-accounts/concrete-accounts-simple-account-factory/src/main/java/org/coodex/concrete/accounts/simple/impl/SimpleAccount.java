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

package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.common.ClassifiableAccountID;
import org.coodex.concrete.common.NamedAccount;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccount implements NamedAccount<ClassifiableAccountID> {

    private final Profile profile;
    private final ClassifiableAccountID id;

    public SimpleAccount(ClassifiableAccountID id) {
        this.profile = Profile.get("/accounts/" + id.getId());
        this.id = id;
    }

    @Override
    public ClassifiableAccountID getId() {
        return id;
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<>(Common.arrayToSet(profile.getStrList("roles")));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getName() {
        return profile.getString("name");
    }


    public String getPassword() {
        return profile.getString("password");
    }

    public String getAuthKey() {
        return profile.getString("authKey");
    }
}
