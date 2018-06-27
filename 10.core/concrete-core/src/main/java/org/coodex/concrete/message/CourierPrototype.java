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

import java.io.Serializable;
import java.lang.reflect.Type;

public abstract class CourierPrototype<M extends Serializable> implements Courier<M> {
    private final String destination;
    private final String queue;
    private final Type topicType;
    private AbstractTopicPrototype<M> topic;

    public CourierPrototype(String queue, String destination, Type topicType) {
        this.destination = destination;
        this.queue = queue;
        this.topicType = topicType;
    }


    @Override
    public void associate(AbstractTopicPrototype<M> topic) {
        this.topic = topic;
    }

    protected AbstractTopicPrototype<M> getTopic() {
        return topic;
    }

    public Type getTopicType() {
        return topicType;
    }

    public String getDestination() {
        return destination;
    }

    public String getQueue() {
        return queue;
    }
}
