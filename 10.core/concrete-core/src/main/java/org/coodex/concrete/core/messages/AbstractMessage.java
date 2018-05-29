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

import org.coodex.concrete.common.messages.Message;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.NotSerializableException;
import java.io.Serializable;

@Deprecated
public abstract class AbstractMessage<T> implements Message<T> {
    private transient final static Logger log = LoggerFactory.getLogger(AbstractMessage.class);

    private final String id = Common.getUUIDStr();
    private final String host = null; // TODO
    private final String subject;
    private final T body;

    public AbstractMessage(String subject, T body) {
        this.subject = subject;
        this.body = body;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getBody() {
        try {
            if (body instanceof Serializable)
                return (T) Common.deepCopy((Serializable) body);
        } catch (NotSerializableException e) {
        } catch (Throwable throwable) {
            log.warn("Unable copy message body.{}", throwable.getLocalizedMessage(), throwable);
        }
        return body;
    }
}
