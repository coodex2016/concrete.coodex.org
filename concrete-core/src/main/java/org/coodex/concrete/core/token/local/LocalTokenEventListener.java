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

package org.coodex.concrete.core.token.local;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.TokenEventListener;

import static org.coodex.concrete.core.token.local.LocalTokenManager.TOKENS;

/**
 * Created by davidoff shen on 2017-05-20.
 */
public class LocalTokenEventListener implements TokenEventListener {
    @Override
    public boolean accept(Token.Event param) {
        return Token.Event.INVALIDATED.equals(param);
    }

    @Override
    public void before(Token token) {

    }

    @Override
    public void after(Token token) {
        if (TOKENS.containsKey(token.getTokenId())) TOKENS.remove(token.getTokenId());
    }
}
