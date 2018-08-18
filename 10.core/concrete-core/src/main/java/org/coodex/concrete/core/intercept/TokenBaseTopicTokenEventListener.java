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

package org.coodex.concrete.core.intercept;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.TokenEventListener;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.message.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenBaseTopicTokenEventListener implements TokenEventListener {
    private final static Logger log = LoggerFactory.getLogger(TokenBaseTopicTokenEventListener.class);

    private static Map<String, Set<Subscription>> subscriptionsMap = new HashMap<String, Set<Subscription>>();


    public static void putSubscription(Subscription subscription) {
        if (subscription == null) return;

        Token token = TokenWrapper.getInstance();
        Set<Subscription> set = null;
        if (!subscriptionsMap.containsKey(token.getTokenId())) {
            synchronized (subscriptionsMap) {
                if (!subscriptionsMap.containsKey(token.getTokenId())) {
                    set = new HashSet<Subscription>();
                    subscriptionsMap.put(token.getTokenId(), set);
                } else {
                    set = subscriptionsMap.get(token.getTokenId());
                }
            }
        } else {
            set = subscriptionsMap.get(token.getTokenId());
        }
        set.add(subscription);
    }


    @Override
    public void before(Token token) {
        if (subscriptionsMap.containsKey(token.getTokenId())) {
            synchronized (subscriptionsMap) {
                if (subscriptionsMap.containsKey(token.getTokenId())) {
                    Set<Subscription> set = subscriptionsMap.get(token.getTokenId());
                    if (set != null) {
                        for (Subscription subscription : set) {
                            subscription.cancel();
                        }
                    }
                    log.debug("token {} cancel {} subscription(s).", token.getTokenId(), set.size());
                    subscriptionsMap.remove(token.getTokenId());
                }
            }
        }
    }


    @Override
    public void after(Token token) {

    }

    @Override
    public boolean accept(Token.Event param) {
        return Token.Event.INVALIDATED.equals(param);
    }
}
