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

public class LocalCourierPrototype<M extends Serializable> extends CourierPrototype<M> {


    public LocalCourierPrototype(String queue, String destination, Type topicType) {
        super(queue, destination, topicType);
    }

    @Override
    protected void afterTopicAssociation() {

    }

    @Override
    public void deliver(M message) {
        getTopic().notify(message);
    }

    @Override
    public boolean isConsumer() {
        return true;
    }

    @Override
    public void setConsumer(boolean consumer) {

    }
}
