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

package org.coodex.concrete.support.dubbo;

import org.apache.dubbo.rpc.RpcContext;
import org.coodex.concrete.common.Caller;

import static org.coodex.concrete.common.ConcreteHelper.AGENT_KEY;

public class ApacheDubboCaller implements Caller {

    private final String address;
    private final String provider;

    public ApacheDubboCaller(RpcContext context) {
        provider = context.getAttachment(AGENT_KEY);
        address = context.getRemoteAddressString();
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getClientProvider() {
        return provider;
    }
}
