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

import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.common.ConcreteHelper.getScheduler;

public class TBMContainer {

    private final static Logger log = LoggerFactory.getLogger(TBMContainer.class);


    //    private static Singleton<ScheduledExecutorService> scheduledExecutor = new Singleton<ScheduledExecutorService>(
//            new Singleton.Builder<ScheduledExecutorService>() {
//                @Override
//                public ScheduledExecutorService build() {
//                    return ExecutorsHelper.newScheduledThreadPool(1);
//                }
//            }
//    );
    private static TBMContainer tbmContainer = new TBMContainer();
    private static SingletonMap<String, TBMQueue> queues = SingletonMap.<String, TBMQueue>builder().function(key -> new TBMQueue()).build();


    private TBMContainer() {
    }

    public static TBMContainer getInstance() {
        return tbmContainer;
    }

    private static void remove(String tokenId, TBMMessage message) {
        if (Common.isBlank(tokenId)) return;
        queues.get(tokenId).remove(message);
        if (log.isDebugEnabled()) {
            log.debug("removed from token {}\n{}", tokenId,
                    JSONSerializerFactory.getInstance().toJson(message.message));
        }
    }

    void push(TokenBasedTopicPrototype.TokenConfirm<?> tokenConfirm, Topic<TokenBasedTopicPrototype.ConsumedNotify> consumedNotifyTopic) {
        if (Common.isBlank(tokenConfirm.getTokenId())) return;
        queues.get(tokenConfirm.getTokenId()).put(new TBMMessage(tokenConfirm, consumedNotifyTopic));
    }

    void remove(TokenBasedTopicPrototype.ConsumedNotify consumedNotify) {
        queues.get(consumedNotify.getTokenId()).remove(consumedNotify.getId());
    }

    public List<ServerSideMessage<?>> getMessages(String tokenId, long timeOut) {
        if (Common.isBlank(tokenId)) {
            if (timeOut > 0) {
                Common.sleep(timeOut);
//                Object lock = new Object();
//                synchronized (lock) {
//                    try {
////                        lock.wait(timeOut);
//                        Clock.objWait(lock, timeOut);
//                    } catch (InterruptedException e) {
//                        log.warn(e.getLocalizedMessage(), e);
//                        Thread.currentThread().interrupt();
//                    }
//                }
            }
            return new ArrayList<>();
        } else {
            List<TBMMessage> messageList = queues.get(tokenId).peekAll(timeOut);

            List<ServerSideMessage<?>> messages = new ArrayList<>();
            for (TBMMessage message : messageList) {
//            message.consumedNotifyTopic.publish(new TokenBasedTopicPrototype.ConsumedNotify(message.id, tokenId));
                message.consumeBy(tokenId);
                messages.add(new SSMImpl(message));
            }

            return messages;
        }
    }

    public List<ServerSideMessage<?>> getMessages(long timeOut) {
        String tokenId = TokenWrapper.getInstance().getTokenId();
        return getMessages(tokenId, timeOut);
    }

    public void listen(String tokenId, TBMListener tbmListener) {
        TBMQueue queue = queues.get(tokenId);
        queue.tbmListener = tbmListener;
    }

    public void clear(String tokenId) {
        TBMQueue queue = queues.remove(tokenId);
        if (queue != null) {
            queue.tbmListener = null;
            queue.peekAll(0);
        }
    }

    public interface TBMListener {
        String getTokenId();

        void onMessage(ServerSideMessage<?> serverSideMessage);
    }

    static class SSMImpl implements ServerSideMessage<Object> {
        private String subject = null;
        private String host = null;
        private String id;
        private Object body;

        SSMImpl(TBMMessage message) {
            this.id = message.id;
//            this.host = // TODO 获得当前主机编号
            this.body = message.message;
            if (message.message instanceof Subject) {
                this.subject = ((Subject) message.message).getSubject();
            } else if (message.message != null) {
                Class<?> clz = message.message.getClass();
                MessageSubject messageSubject = clz.getAnnotation(MessageSubject.class);
                if (messageSubject != null) {
                    this.subject = messageSubject.value();
                }
            }
        }


        @Override
        public String getSubject() {
            return subject;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public Object getBody() {
            return body;
        }
    }


    static class TBMMessage {
        private Object message;
        private String id;
        private Future<?> future;
        private Topic<TokenBasedTopicPrototype.ConsumedNotify> consumedNotifyTopic;


        public TBMMessage(final TokenBasedTopicPrototype.TokenConfirm<?> tokenConfirm, Topic<TokenBasedTopicPrototype.ConsumedNotify> consumedNotifyTopic) {
            this.consumedNotifyTopic = consumedNotifyTopic;
            this.id = tokenConfirm.getId();
            this.message = tokenConfirm.getMessage();
            this.future = getScheduler("tbm").schedule(
                    () -> remove(tokenConfirm.getTokenId(), TBMMessage.this),
                    Config.getValue("tokenBasedTopicMessage.cacheLife", 30, getAppSet()),
                    TimeUnit.SECONDS); //默认30秒失效
        }

        void consumeBy(String tokenId) {
            consumedNotifyTopic.publish(new TokenBasedTopicPrototype.ConsumedNotify(id, tokenId));
        }
    }

    static class TBMQueue {
        private final Queue<TBMMessage> queue = new LinkedBlockingQueue<>();
        private final Object lock = new Object();
        private Map<String, TBMMessage> index = new ConcurrentHashMap<>();
        private TBMListener tbmListener;

        void remove(TBMMessage message) {
            if (queue.contains(message)) {
                synchronized (queue) {
                    if (queue.contains(message)) {
                        queue.remove(message);
                        index.remove(message.id);
                    }
                }
            }
        }

        public void put(TBMMessage message) {
            synchronized (queue) {
                if (tbmListener != null) {
                    message.consumeBy(tbmListener.getTokenId());
                    tbmListener.onMessage(new SSMImpl(message));
                } else {
                    queue.add(message);
                    index.put(message.id, message);
                }
                synchronized (lock) {
                    queue.notifyAll();
                }

            }
        }

        public void remove(String id) {
            if (index.containsKey(id)) {
                synchronized (queue) {
                    remove(index.get(id));
                }
            }
        }


        public List<TBMMessage> peekAll(long timeOut) {
            List<TBMMessage> list = null;

            if (queue.size() == 0 && timeOut > 0) {
                synchronized (queue) {
                    try {
                        queue.wait(timeOut);
                    } catch (InterruptedException e) {
                        log.warn(e.getLocalizedMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (queue.size() > 0) {
                synchronized (lock) {
                    list = Arrays.asList(queue.toArray(new TBMMessage[0]));
                    queue.clear();
                    index.clear();
                    for (TBMMessage message : list) {
                        message.future.cancel(true);
                    }
                }
            }

            return list == null ? Collections.emptyList() : list;
        }
    }

}
