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

import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.common.messages.MessageFilter;
import org.coodex.concrete.core.token.TokenManager;

public abstract class TokenBaseMessageFilter<T> implements MessageFilter<T> {

    private final String tokenId;

    public TokenBaseMessageFilter(Token token) {
        if (token == null || !token.isValid())
            throw new RuntimeException("invalid token.");
        tokenId = token.getTokenId();
    }

    private Token getToken() {
        return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(tokenId, false);
    }

    @Override
    public boolean iWantIt(Message<? extends T> message) {
        Token token = getToken();
        if (token == null || !token.isValid()) return false;
        return iWantIt(message, token);
    }

    protected abstract boolean iWantIt(Message<? extends T> message, Token token);


}
