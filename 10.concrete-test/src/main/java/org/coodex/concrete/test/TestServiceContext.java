/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.Token;
import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.SIDE_TEST;

public class TestServiceContext extends ServiceContext {

    private class TestCaller implements Caller{
        @Override
        public String getAddress() {
            return getIp(token);
        }

        @Override
        public String getAgent() {
            return "Concrete-test 0.2.1-SNAPSHOT";
        }
    }

    private static Map<String, String> address = new HashMap<String, String>();

    private synchronized String getIp(Token token) {
        String ip = address.get(token.getTokenId());
        if (ip == null) {
            ip = mockIp();
            address.put(token.getTokenId(), ip);
        }
        return ip;
    }

    private String mockIp() {
        return String.format("%d.%d.%d.%d",
                Common.random(1, 254),
                Common.random(1, 254),
                Common.random(1, 254),
                Common.random(1, 254));
    }

    public TestServiceContext(Token token, Subjoin subjoin) {
        this.token = token;
        this.model = "TEST";
        this.caller = new TestCaller();
        this.side = SIDE_TEST;
        this.subjoin = subjoin;
    }
}
