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

import org.coodex.config.Config;
import org.coodex.util.Common;

import java.lang.reflect.Type;
import java.util.Objects;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.util.ReflectHelper.typeToCodeStr;

public class TopicKey {
    String queue;
    Type topicType;
    String topicTypeName;

    public TopicKey(String queue, Type topicType) {
        this.queue = Common.isBlank(queue) ?
                Config.get("queue.default", getAppSet()) : queue;
        if (Common.isBlank(queue))
            this.queue = null;
        this.topicType = topicType;
        this.topicTypeName = typeToCodeStr(topicType);
    }

    static TopicKey copy(TopicKey topicKey) {
        return new TopicKey(topicKey.queue, topicKey.topicType);
    }

    public String getQueue() {
        return queue;
    }

    public Type getTopicType() {
        return topicType;
    }

    public String getTopicTypeName() {
        return topicTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !TopicKey.class.isAssignableFrom(o.getClass())) return false;
        TopicKey topicKey = (TopicKey) o;
        return Objects.equals(queue, topicKey.queue) && Objects.equals(topicTypeName, topicKey.topicTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queue, topicTypeName);
    }

    @Override
    public String toString() {
        return "TopicKey{" +
                "queue='" + queue + '\'' +
                ", topicType=" + topicType +
                ", topicTypeName='" + topicTypeName + '\'' +
                '}';
    }
}
