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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.core.messages.Courier;

import static org.coodex.concrete.common.ConcreteContext.SIDE_SERVER;

public class JAXRSServiceContext extends ServiceContext {

    private final static Courier jaxrsCourier = new JaxRSCourier();

    JAXRSServiceContext(Caller caller, Token token, AbstractUnit unit, Subjoin subjoin) {
        this.caller = caller;
        this.token = token;
        this.currentUnit = unit;
        this.subjoin = subjoin;
        this.side = SIDE_SERVER;
        this.model = JaxRSHelper.JAXRS_MODEL;
        this.courier = jaxrsCourier;
    }
}
