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

package org.coodex.concrete.test;

import org.coodex.concrete.common.AbstractContainerContext;
import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.Token;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import static org.coodex.concrete.common.ConcreteHelper.VERSION;
import static org.coodex.concrete.core.token.TokenWrapper.newToken;

public class TestServiceContext extends AbstractContainerContext implements org.coodex.concrete.common.TestServiceContext {


    private static SingletonMap<String, Token> tokens =
            new SingletonMap<String, Token>(new SingletonMap.Builder<String, Token>() {
                @Override
                public Token build(final String key) {
                    return newToken();
                }
            });

    public TestServiceContext(String tokenId, Subjoin subjoin) {
        super(new TestCaller(), getTestToken(tokenId), subjoin, null);
    }

    private static String mockIp() {
        return String.format("%d.%d.%d.%d",
                Common.random(1, 254),
                Common.random(1, 254),
                Common.random(1, 254),
                Common.random(1, 254));
    }

    private static Token getTestToken(String tokenId) {
        return tokens.get(Common.isBlank(tokenId) ?
                Common.getUUIDStr() :
                tokenId);
    }

    private static class TestCaller implements Caller {
        @Override
        public String getAddress() {
            return mockIp();
        }

        @Override
        public String getClientProvider() {
            return "concrete-test " + VERSION;
        }
    }
}
