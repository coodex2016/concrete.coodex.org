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

package org.coodex.concrete.client;

import org.coodex.concrete.common.*;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.util.Common;

import static org.coodex.concrete.common.ConcreteContext.SIDE_CLIENT;

public abstract class ClientServiceContext extends ServiceContext {

    private String tokenId;
    private Destination destination;

    public ClientServiceContext(Destination destination,RuntimeContext context) {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        this.side = SIDE_CLIENT;
        this.destination = destination;
        this.currentUnit = getUnit(context);
        if (serviceContext != null) {
            try {
                this.setTokenId(serviceContext.getToken().getTokenId());
            } catch (ConcreteException ce) {
                if (ce.getCode() != ErrorCodes.NONE_TOKEN) {
                    throw ce;
                }
            } catch (NullPointerException npe){

            }
        }
    }

    protected abstract AbstractUnit getUnit(RuntimeContext context);

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public Destination getDestination() {
        return destination;
    }

    @Override
    public Token getToken() {
        return Common.isBlank(getTokenId()) ? null :
                BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(getTokenId());
    }


}
