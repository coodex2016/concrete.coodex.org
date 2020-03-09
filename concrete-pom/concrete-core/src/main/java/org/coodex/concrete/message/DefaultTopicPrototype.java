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

import org.coodex.concrete.common.IF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DefaultTopicPrototype<M extends Serializable> extends AbstractTopicPrototype<M> {
    private final static Logger log = LoggerFactory.getLogger(AbstractTopicPrototype.class);


    public DefaultTopicPrototype(Courier<M> courier) {
        super(courier);
    }

    private boolean sending = false;

    @Override
    public void publish(final M message) {
        IF.isNull(message, "message MUST NOT null.");
        if (!sending) {
            try {
                sending = true;
                //noinspection unchecked
                getCourier().deliver(message);
            } finally {
                sending = false;
            }
        }
    }


}
