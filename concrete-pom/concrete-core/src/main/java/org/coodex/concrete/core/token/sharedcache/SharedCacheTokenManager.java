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

package org.coodex.concrete.core.token.sharedcache;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.config.Config;
import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientManager;
import org.coodex.util.UUIDHelper;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.common.ConcreteHelper.getTokenMaxIdleInMinute;

/**
 * Created by davidoff shen on 2016-11-23.
 */
@SuppressWarnings("unused")
public class SharedCacheTokenManager implements TokenManager {


    @Override
    public Token getToken(String id) {
        return getTokenById(id);
    }

//    @Override
//    @Deprecated
//    public Token getToken(String id, boolean force) {
//
//        return $getToken(id);
//    }

    private Token getTokenById(String id) {
        String tokenCacheType = Config.get("tokenCacheType", getAppSet());
        // ConcreteHelper.getProfile().getString("tokenCacheType");
        SharedCacheClient client = SharedCacheClientManager.getClient(tokenCacheType);

        long maxIdleTime = getTokenMaxIdleInMinute() * 60L * 1000L;

        return new SharedCacheToken(client, id, maxIdleTime);
    }

    @Override
    public Token newToken() {
        return getTokenById(UUIDHelper.getUUIDString());
    }
}
