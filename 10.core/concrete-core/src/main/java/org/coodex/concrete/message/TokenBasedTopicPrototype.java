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

package org.coodex.concrete.message;

import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Set;

/**
 * @param <M>
 */
public class TokenBasedTopicPrototype<M extends Serializable> extends AbstractTopicPrototype<M> implements TokenBasedTopic<M> {

    private final static Logger log = LoggerFactory.getLogger(TokenBasedTopicPrototype.class);

    /**
     * 队列1，消息发布时，为消息指定id，进行广播
     */
    private Topic<Id<M>> idWrapper
            = Topics.<Id<M>, Topic<Id<M>>>get(new GenericTypeHelper.GenericType<Topic<Id<M>>>(getClass()) {
    }.getType(), getQueue());

    /**
     * 队列2，队列1消息到达后，当前节点判定是否可被已注册的订阅者消费，确认的扔到队列2
     */
    private Topic<TokenConfirm<M>> tokenConfirmTopic
            = Topics.<TokenConfirm<M>, Topic<TokenConfirm<M>>>get(new GenericTypeHelper.GenericType<Topic<TokenConfirm<M>>>(getClass()) {
    }.getType(), getQueue());


    /**
     * 队列3，消息被消费后，通知其他节点
     */
    private Topic<ConsumedNotify> consumedNotifyTopic
            = Topics.<ConsumedNotify, Topic<ConsumedNotify>>get(new GenericTypeHelper.GenericType<Topic<ConsumedNotify>>() {
    }.getType(), getQueue());


    public TokenBasedTopicPrototype(Courier<M> courier) {
        super(courier);
        // 订阅三个队列中的数据，并作出反应

        idWrapper.subscribe(new Observer<Id<M>>() {
            @Override
            public void update(Id<M> message) throws Throwable {
                // 判定该消息是否在当前主机内被订阅
                doFilter(message);
            }
        });

        tokenConfirmTopic.subscribe(new Observer<TokenConfirm<M>>() {
            @Override
            public void update(TokenConfirm<M> message) throws Throwable {
                TBMContainer.getInstance().push(message, consumedNotifyTopic);
            }
        });

        consumedNotifyTopic.subscribe(new Observer<ConsumedNotify>() {
            @Override
            public void update(ConsumedNotify message) throws Throwable {
                TBMContainer.getInstance().remove(message);
            }
        });


    }

    private void doFilter(Id<M> message) {
        Set<Observer<M>> observers = getObservers();
        for (Observer<M> observer : observers) {
            if (observer instanceof AbstractTokenBasedMessageFilter) {
                AbstractTokenBasedMessageFilter<M> messageFilter = (AbstractTokenBasedMessageFilter<M>) observer;
                try {
                    if (messageFilter.handle(message.getMessage())) {
                        // todo token失效怎么办？
                        tokenConfirmTopic.publish(new TokenConfirm<M>(messageFilter.getTokenId(), message));
                    }
                } catch (Throwable th) {
                    log.warn("filter failed: {}", th.getLocalizedMessage(), th);
                }
            } else {
                log.warn("WTF {} ?", observer);
            }
        }
    }

    @Override
    public void notify(M message) {
        // do nothing
    }


    @Override
    public Subscription subscribe(Observer<M> observer) {
        if (observer instanceof MessageFilter) {
            return subscribe((MessageFilter<M>) observer);
        } else {
            throw new RuntimeException("TokenBasedTopic must subscribed by a MessageFilter");
        }

    }

    @Override
    public void publish(M message) {
        idWrapper.publish(new Id<M>(message));
    }


    @Override
    public Subscription subscribe(MessageFilter<M> messageFilter) {
        return super.subscribe(getObserver(messageFilter));
    }

    private Observer<M> getObserver(final MessageFilter<M> messageFilter) {
        return (messageFilter instanceof AbstractTokenBasedMessageFilter) ?
                (AbstractTokenBasedMessageFilter<M>) messageFilter :
                new AbstractTokenBasedMessageFilter<M>() {
                    @Override
                    public boolean handle(M message) {
                        return messageFilter.handle(message);
                    }
                };
    }


    public static class ConsumedNotify implements Serializable {
        private String id;
        private String tokenId;

        public ConsumedNotify() {
        }

        public ConsumedNotify(String id, String tokenId) {
            this.id = id;
            this.tokenId = tokenId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }
    }

    public static class Id<M extends Serializable> implements Serializable {
        private String id;
        private M message;

        public Id() {
        }

        public Id(M message) {
            this.id = Common.getUUIDStr();
            this.message = message;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public M getMessage() {
            return message;
        }

        public void setMessage(M message) {
            this.message = message;
        }
    }

    public static class TokenConfirm<M extends Serializable> extends Id<M> {

        public TokenConfirm() {
        }

        private String tokenId;

        TokenConfirm(String tokenId, Id<M> message) {
            this.tokenId = tokenId;
            this.setId(message.getId());
            this.setMessage(message.getMessage());
        }

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }
    }
}
