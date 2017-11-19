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

package org.coodex.concrete.jaxrs.client;

import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.common.SubjoinWrapper;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.jaxrs.JaxRSHelper;

import javax.ws.rs.core.HttpHeaders;

import static org.coodex.concrete.common.ConcreteContext.SIDE_CLIENT;
import static org.coodex.concrete.common.ConcreteHelper.VERSION;

public class JaxrsClientServiceContext extends ServiceContext {

    private static final String CONCRETE_JAXRS_USER_AGENT = "cocnrete-jaxrs-client " + VERSION;

    private static Caller CLIENT_CALLER = new Caller() {
        @Override
        public String getAddress() {
            return Caller.LOCAL_CALLER;
        }

        @Override
        public String getAgent() {
            return CONCRETE_JAXRS_USER_AGENT;
        }
    };

    public JaxrsClientServiceContext(AbstractUnit unit) {
        this(unit,SIDE_CLIENT);
    }

    public JaxrsClientServiceContext(AbstractUnit unit, Integer side) {
        this.caller = CLIENT_CALLER;
        this.side = side;
        this.model = JaxRSHelper.JAXRS_MODEL;
        this.currentUnit = unit;
        this.token = TokenWrapper.getInstance();
        this.subjoin = new SubjoinWrapper.DefaultSubjoin();
        this.subjoin.add(HttpHeaders.USER_AGENT, CONCRETE_JAXRS_USER_AGENT);
    }
}
