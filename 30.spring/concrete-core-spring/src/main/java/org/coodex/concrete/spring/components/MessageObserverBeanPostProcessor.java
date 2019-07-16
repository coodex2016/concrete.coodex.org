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

import org.coodex.concrete.message.*;
import org.coodex.util.GenericTypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import javax.inject.Named;
import java.lang.reflect.Type;

@Named
public class MessageObserverBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    private final static Logger log = LoggerFactory.getLogger(MessageObserverBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Observer) {
            Observer observer = (Observer) bean;
            getTopic(observer, beanName).subscribe(observer);
        }
        return super.postProcessAfterInitialization(bean, beanName);
    }

    private AbstractTopic getTopic(Observer observer, String beanName) {
        MessageConsumer messageConsumer = observer.getClass().getAnnotation(MessageConsumer.class);
        Type topicType = null;
        String queue = null;
        final Type messageType = GenericTypeHelper.solve(Observer.class.getTypeParameters()[0], observer.getClass());

        Class<? extends AbstractTopic> topicClass = messageConsumer == null ? Topic.class :
                messageConsumer.topicType();
        queue = messageConsumer == null ? null : messageConsumer.queue();

        if (topicClass.equals(Topic.class) || topicClass.equals(AbstractTopic.class)) {
            topicType = GenericTypeHelper.buildParameterizedType(Topic.class, messageType);
        } else {
            topicType = topicClass;
        }
        log.debug("{} subscribe {}, {}", topicType, beanName, observer.getClass().getName());
        return Topics.get(topicType, queue);
    }
}
