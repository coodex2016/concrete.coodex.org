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
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.message.Subscription;
import org.coodex.concrete.message.TBMContainer;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

class TBTSManager {
    private final static Logger log = LoggerFactory.getLogger(TBTSManager.class);

//    private static Map<String, Set<Subscription>> subscriptionsMap = new HashMap<String, Set<Subscription>>();

    private static final SingletonMap<String, Set<Subscription>> subscriptionsMap
            = SingletonMap.<String, Set<Subscription>>builder().function(key -> new HashSet<>()).build();


    static void putSubscription(Subscription subscription) {
        if (subscription == null) return;
        Token token = TokenWrapper.getInstance();
        subscriptionsMap.get(token.getTokenId()).add(subscription);
    }

    static Object tokenLock(Token token) {
        return subscriptionsMap.get(token.getTokenId());
    }

    static void cancel(Token token) {
        Set<Subscription> set = subscriptionsMap.get(token.getTokenId());
        if (set.size() != 0) {
            synchronized (subscriptionsMap) {
                for (Subscription subscription : set) {
                    subscription.cancel();
                }
                TBMContainer.getInstance().clear(token.getTokenId());
                subscriptionsMap.remove(token.getTokenId());
                log.debug("token {} cancel {} subscription(s).", token.getTokenId(), set.size());
            }
        }


//        if (subscriptionsMap.containsKey(token.getTokenId())) {
//            synchronized (subscriptionsMap) {
//                if (subscriptionsMap.containsKey(token.getTokenId())) {
//                    Set<Subscription> set = subscriptionsMap.get(token.getTokenId());
//                    if (set != null) {
//                        for (Subscription subscription : set) {
//                            subscription.cancel();
//                        }
//                    }
//                    TBMContainer.getInstance().clear(token.getTokenId());
//                    subscriptionsMap.remove(token.getTokenId());
//                    log.debug("token {} cancel {} subscription(s).", token.getTokenId(), set.size());
//                }
//            }
//        }
    }
}
