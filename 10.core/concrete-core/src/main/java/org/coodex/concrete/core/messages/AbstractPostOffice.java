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

package org.coodex.concrete.core.messages;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.common.messages.PostOffice;
import org.coodex.concrete.common.messages.Subscriber;
import org.coodex.concrete.common.messages.Subscription;
import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public abstract class AbstractPostOffice implements PostOffice {

    private final ExecutorService executorService = ConcreteHelper.getExecutor();
    private final static Logger log = LoggerFactory.getLogger(AbstractPostOffice.class);

    private Set<SubscriberImpl> subscribers = new HashSet<SubscriberImpl>();

    @SuppressWarnings("unchecked")
    protected final <T> void distribute(final Message<T> message) {
        if (message == null) return;
        for (final SubscriberImpl subscriber : subscribers) {
            if (accept(message, subscriber.getSubscription())) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            subscriber.<T>getSubscription().onMessage(message);
                        } catch (Throwable throwable) {
                            log.warn("message [{}:{}] distribute failed.{}",
                                    message.getSubject(), message.getId(), throwable.getLocalizedMessage(),
                                    throwable);
                        }
                    }
                });

            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean accept(Message message, Subscription subscription) {
        if (!message.getSubject().equals(subscription.getSubject())) return false;
        try {
            return subscription.getFilter() == null || subscription.getFilter().iWantIt(message);
        } catch (ClassCastException cce) {
            return false;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> Subscriber<T> subscribe(Subscription<T> subscription) {
        if (subscription == null) throw new NullPointerException("subscription is null.");

        for (SubscriberImpl s : subscribers) {
            if (s.getSubscription().isSame(subscription)) {
                return s;
            }
        }

        SubscriberImpl<T> subscriber = new SubscriberImpl<T>(subscription);
        subscribers.add(subscriber);
        return subscriber;
    }

    @Override
    public synchronized void cancel(Subscriber subscriber) {
        if (subscriber instanceof SubscriberImpl)
            subscribers.remove(subscriber);
    }
}
