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

package org.coodex.concrete.core.token.local;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 本地会话管理<br/>
 * 可在concrete.properties 中设置 localTokenManager.maxIdleTime，单位为分钟，必须为大于0的整数，默认60分钟
 *
 * <S>增加集群支持</S> 2016-11-22，利用分布式TokenManager完成（额外添加）
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
public class LocalTokenManager implements TokenManager {

    static final Map<String, TokenWithFuture> TOKENS = new ConcurrentHashMap<String, TokenWithFuture>();

    private static final ScheduledExecutorService EXECUTOR = ExecutorsHelper.newSingleThreadScheduledExecutor();

    @Override
    public Token getToken(String id) {
        return getToken(id, false);
    }

    @Override
    @Deprecated
    public Token getToken(final String id, boolean force) {
        return buildToken(id, false);
    }

    private synchronized Token buildToken(String id, boolean force) {
        if (id == null) throw new NullPointerException("token id could NOT be NULL.");
        TokenWithFuture tokenWithFuture = TOKENS.get(id);
        if (tokenWithFuture == null && force) {
            tokenWithFuture = new TokenWithFuture(new LocalToken(id));
            TOKENS.put(id, tokenWithFuture);
        }

        if (tokenWithFuture != null) {
            tokenWithFuture.active();
            return tokenWithFuture.token;
        }
        return null;
    }

    @Override
    public Token newToken() {
        return buildToken(Common.getUUIDStr(), true);
    }

    private static class TokenWithFuture {

        LocalToken token;

        private ScheduledFuture<?> handler = null;

        public TokenWithFuture(LocalToken token) {
            this.token = token;
        }

        void active() {

            long maxIdleTime = ConcreteHelper.getProfile().getLong("localTokenManager.maxIdleTime", DEFAULT_MAX_IDLE) * 60 * 1000;
            if (maxIdleTime <= 0) {
                maxIdleTime = DEFAULT_MAX_IDLE * 60 * 1000;
            }

            Runnable future = new Runnable() {
                @Override
                public void run() {
                    token.invalidate();
                }
            };

            if (!EXECUTOR.isTerminated() && !EXECUTOR.isShutdown()) {
                if (handler == null) { //新建
                    handler = EXECUTOR.schedule(future, maxIdleTime, TimeUnit.MILLISECONDS);
                } else {
                    if (token.isValid()) {
                        token.active();
                        try {
                            handler.cancel(true);
                        } finally {
                            handler = EXECUTOR.schedule(future, maxIdleTime, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }
        }

    }
}
