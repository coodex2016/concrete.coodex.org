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

package org.coodex.concrete.support.amqp;

import org.coodex.concrete.amqp.AMQPConstants;
import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.Subjoin;
import org.coodex.util.Common;

public class AMQPCaller implements Caller {
    private final String clientProvider;

    public AMQPCaller(Subjoin subjoin) {
        String client = subjoin.get(AMQPConstants.SUBJOIN_KEY_CLIENT_PROVIDER);
        clientProvider = Common.isBlank(client) ? "unkown" : client;
    }

    @Override
    public String getAddress() {
        return "N/A";
    }

    @Override
    public String getClientProvider() {
        return clientProvider;
    }
}
