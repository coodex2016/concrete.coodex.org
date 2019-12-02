/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.coodex.concrete.message.AggregatedCourierPrototypeProvider.AGGREGATED;

public abstract class AggregatedCourierPrototype<M extends Serializable> extends CourierPrototype<M> implements AggregatedCourier {

    private final static Logger log = LoggerFactory.getLogger(AggregatedCourierPrototype.class);
    private Set<Topic<M>> topics = new LinkedHashSet<>();

    public AggregatedCourierPrototype(String queue, String destination, Type topicType) {
        super(queue, destination, topicType);
        Common.toArray(
                destination.substring(AGGREGATED.length() + 1),
                ",",
                new ArrayList<>()
        ).forEach(q -> {
            String a = q.trim();
            if (!Common.isBlank(a)) return;
            topics.add(Topics.get(topicType, a));
        });
        if (topics.size() == 0) {
            throw new RuntimeException("none topic queue aggregated.");
        }
    }


    @Override
    protected void afterTopicAssociation() {

    }

    @Override
    public void deliver(M message) {
        for (Topic<M> topic : topics) {
            try {
                topic.publish(message);
            } catch (Throwable th) {
                log.warn("aggregated send failed: {}", th.getLocalizedMessage(), th);
            }
        }
    }

    @Override
    public boolean isConsumer() {
        return false;
    }

    @Override
    public void setConsumer(boolean consumer) {

    }
}
