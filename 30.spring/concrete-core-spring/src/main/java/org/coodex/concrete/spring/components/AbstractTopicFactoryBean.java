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

import org.coodex.concrete.message.Queue;
import org.coodex.concrete.message.Topics;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.coodex.concrete.message.GenericTypeHelper.solve;

public abstract class AbstractTopicFactoryBean<T> implements FactoryBean<T> {

    private Type topicType = null;

    protected Type getType() {
        if (topicType == null) {
            synchronized (this) {
                if (topicType == null) {
                    topicType = solve(AbstractTopicFactoryBean.class.getTypeParameters()[0], getClass());
                }
            }
        }
        return topicType;
    }


    private boolean queueLoaded = false;
    private String queueName;

    protected String getQueueName() {
        if (!queueLoaded) {
            synchronized (this) {
                if (!queueLoaded) {
                    Queue queue = getClass().getAnnotation(Queue.class);
                    queueName = queue == null ? null : queue.value();
                    queueLoaded = true;
                }
            }
        }
        return queueName;
    }


    @Override
    public Class<?> getObjectType() {
        ParameterizedType pt = (ParameterizedType) getType();
        return (Class<?>) pt.getRawType();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public T getObject() throws Exception {
        return Topics.get(getType(), getQueueName());
    }
}
