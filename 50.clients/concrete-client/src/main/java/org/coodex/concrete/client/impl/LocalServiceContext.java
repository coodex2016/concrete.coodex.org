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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.client.ClientServiceContext;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.struct.AbstractUnit;

import static org.coodex.concrete.common.AModule.getUnit;
import static org.coodex.concrete.common.ConcreteContext.SIDE_LOCAL_INVOKE;


public class LocalServiceContext extends ClientServiceContext {
    public LocalServiceContext(Destination destination, RuntimeContext context) {
        super(destination, context);
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        this.caller = serviceContext.getCaller();
        this.courier = serviceContext.getCourier();
        this.logging = serviceContext.getLogging();
        this.model = serviceContext.getModel();
        this.subjoin = serviceContext.getSubjoin();
        this.side = SIDE_LOCAL_INVOKE;
        this.token = serviceContext.getToken();
    }

    @Override
    protected AbstractUnit getUnit(RuntimeContext context) {
        return AModule.getUnit(context.getDeclaringClass(), context.getDeclaringMethod());
    }


}
