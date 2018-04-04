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

package test.org.coodex.concrete.messages;

import com.alibaba.fastjson.JSON;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.common.messages.MessageFilter;
import org.coodex.concrete.common.messages.Subscription;

public abstract class AbstractSubscription<T> implements Subscription<T> {

    private String subject;
    private MessageFilter<? super T> filter;

    public AbstractSubscription(String subject, MessageFilter<? super T> filter) {
        this.subject = subject;
        this.filter = filter;
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
        return false;
    }

    @Override
    public void onMessage(Message<T> body) {
        System.out.println("Subject: " + subject + "\n" + JSON.toJSONString(body));
    }
}
