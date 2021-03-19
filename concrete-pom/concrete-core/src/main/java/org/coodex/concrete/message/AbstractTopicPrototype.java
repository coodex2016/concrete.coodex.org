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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.IF;
import org.coodex.util.Common;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public abstract class AbstractTopicPrototype<M extends Serializable> implements AbstractTopic<M> {

    private final static Logger log = LoggerFactory.getLogger(AbstractTopicPrototype.class);

    private static final Singleton<Executor> POOL_SINGLETON = Singleton.with(
            () -> ConcreteHelper.getExecutor("topic")
    );

    private final Courier<?> courier;

    private final Map<Observer<M>, SubscriptionImpl> subscriptions = new ConcurrentHashMap<>();

    public AbstractTopicPrototype(Courier<M> courier) {
        this.courier = courier;
        IF.isNull(courier, "courier MUST NOT null.")
                .associate(this);
    }

    protected Set<Observer<M>> getObservers() {
        return Collections.unmodifiableSet(subscriptions.keySet());
    }

    private void remove(Observer<M> observer) {
        if (!subscriptions.containsKey(observer)) {
            return;
        }
        synchronized (subscriptions) {
            subscriptions.remove(observer);

            if (subscriptions.size() == 0) {
                this.courier.setConsumer(false);
            }
        }
    }

    protected String getQueue() {
        return courier instanceof CourierPrototype ?
                ((CourierPrototype<?>) courier).getQueue() : null;
    }

    protected Courier<?> getCourier() {
        return courier;
    }

    protected Executor getExecutor() {
        return POOL_SINGLETON.get();
    }

    @Override
    public Subscription subscribe(Observer<M> observer) {
        if (getCourier() instanceof AggregatedCourier) {
            throw new RuntimeException("Aggregated queue could not subscribe message.");
        }
        if (!subscriptions.containsKey(observer)) {
            synchronized (subscriptions) {
                if (!subscriptions.containsKey(observer)) {
                    subscriptions.put(observer, new SubscriptionImpl(observer));
                    if (!courier.isConsumer()) {
                        courier.setConsumer(true);
                    }
                }
            }
        }
        return subscriptions.get(observer);
    }


    public void notify(final M message) {
        Set<Observer<M>> observers = getObservers();
        for (final Observer<M> observer : observers) {

            final MessageFilter<M> finalFilter = (observer instanceof MessageFilter) ? Common.cast(observer) : null;
            getExecutor().execute(() -> {
                try {
                    if (finalFilter == null || finalFilter.handle(message)) {
                        observer.update(message);
                    }
                } catch (Throwable throwable) {
                    log.warn("message update failed.", throwable);
                }
            });

        }
    }

    private class SubscriptionImpl implements Subscription {

        private final Observer<M> observer;

        private SubscriptionImpl(Observer<M> observer) {
            this.observer = observer;
        }

        @Override
        public void cancel() {
            remove(observer);
        }
    }
}

