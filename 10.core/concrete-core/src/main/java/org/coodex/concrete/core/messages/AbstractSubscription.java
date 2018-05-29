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

import org.coodex.concrete.common.messages.MessageFilter;
import org.coodex.concrete.common.messages.Subscriber;
import org.coodex.concrete.common.messages.Subscription;

@Deprecated
public abstract class AbstractSubscription<T> implements Subscription<T> {
    private final String subject;
    private final MessageFilter<T> filter;
    protected Subscriber<T> subscriber;


    public AbstractSubscription(String subject, MessageFilter<T> filter) {
        this.subject = subject;
        this.filter = filter;
    }

    @Override
    public void setSubscriber(Subscriber<T> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public MessageFilter<? super T> getFilter() {
        return filter;
    }

    @Override
    public boolean isSame(Subscription anotherSubscription) {
        if (this == anotherSubscription) return true;
        if (anotherSubscription == null) return false;

        if (subject != null ? !subject.equals(anotherSubscription.getSubject())
                : anotherSubscription.getSubject() != null) return false;
        return filter != null ? filter.equals(anotherSubscription.getFilter())
                : anotherSubscription.getFilter() == null;
    }

}
