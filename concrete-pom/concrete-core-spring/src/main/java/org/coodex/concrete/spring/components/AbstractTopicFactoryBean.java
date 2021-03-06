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

package org.coodex.concrete.spring.components;

import org.coodex.concrete.message.Topics;
import org.coodex.util.Common;

import java.lang.reflect.Type;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;

@SuppressWarnings("unused")
public abstract class AbstractTopicFactoryBean<T> /*implements FactoryBean<T>*/ {

    private Type topicType = null;
    private boolean queueLoaded = false;
    private String queueName;

    protected Type getType() {
        if (topicType == null) {
            topicType = solveFromInstance(AbstractTopicFactoryBean.class.getTypeParameters()[0], this);
        }
        return topicType;
    }

    public T getActualTopic(String queueName) {
        return Common.cast(Topics.get(getType(), queueName));
    }

    private String getQueueName() {
        if (!queueLoaded) {
            org.coodex.concrete.message.Queue queue =
                    getClass().getAnnotation(org.coodex.concrete.message.Queue.class);
            queueName = queue == null ? null : queue.value();
            queueLoaded = true;
        }
        return queueName;
    }

    private java.lang.reflect.Type getTopicType() {
        if (topicType == null) {
            topicType = getClass().getGenericInterfaces()[0];
        }
        return topicType;
    }

}
