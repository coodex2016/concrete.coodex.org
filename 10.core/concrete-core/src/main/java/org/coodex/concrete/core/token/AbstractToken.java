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

package org.coodex.concrete.core.token;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.TokenEventListener;
import org.coodex.util.AcceptableServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by davidoff shen on 2017-05-20.
 */
public abstract class AbstractToken implements Token {

    private static final AcceptableServiceLoader<Token.Event, TokenEventListener> LISTENER_LOADER
            = new AcceptableServiceLoader<Event, TokenEventListener>(){};

    private final static Logger log = LoggerFactory.getLogger(AbstractToken.class);

    protected final void runListeners(Event event, boolean before) {
        List<TokenEventListener> listeners = LISTENER_LOADER.getServiceInstances(event);
        if (listeners.size() > 0) {
            Token token = new ReadOnlyToken(this);
            for (TokenEventListener listener : listeners) {
                try {
                    if (before)
                        listener.before(token);
                    else
                        listener.after(token);
                } catch (Throwable th) {
                    log.warn("error occurred on {} {} listener  [{}]: {}",
                            before ? "before" : "after",
                            event.toString(), listener.getClass(), th.getLocalizedMessage(), th);
                }
            }
        }
    }


    @Override
    public final void invalidate() {
        runListeners(Event.INVALIDATED, true);
        $invalidate();
        runListeners(Event.INVALIDATED, false);
    }

    protected abstract void $invalidate();


    @Override
    public final void renew() {
        if (isValid()) invalidate();
        runListeners(Event.CREATED, true);
        $renew();
        runListeners(Event.CREATED, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T> T getAttribute(String key) {
        return (T) getAttribute(key, null);
    }


    protected abstract void $renew();
}
