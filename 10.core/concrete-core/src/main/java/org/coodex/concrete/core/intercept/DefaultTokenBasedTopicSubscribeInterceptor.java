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

package org.coodex.concrete.core.intercept;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.message.MessageFilter;
import org.coodex.util.Common;

import javax.inject.Inject;
import java.io.Serializable;

public abstract class DefaultTokenBasedTopicSubscribeInterceptor<M extends Serializable> extends AbstractTokenBasedTopicSubscribeInterceptor<M> {

    private String key = String.format("tbm_%s", Common.getUUIDStr());
    @Inject
    private Token token;

    @Override
    protected final MessageFilter<M> subscribe() {
        MessageFilter<M> mMessageFilter = getMessageFilter();
        if (mMessageFilter != null) {
            try {
                return mMessageFilter;
            } finally {
                token.setAttribute(key, "subscribed");
            }
        } else {
            return null;
        }
    }

    protected abstract MessageFilter<M> getMessageFilter();

    protected abstract boolean accept();

    @Override
    protected boolean check() {
        return token.getAttribute(key, String.class) == null && accept();
    }
}
