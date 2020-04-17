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

import org.coodex.closure.StackClosureContext;
import org.coodex.concrete.common.*;

import java.util.Locale;
import java.util.Map;

public abstract class ClientSideContext implements ServiceContext {
    public static final StackClosureContext<Map<String, String>> SUBJOIN_CONTEXT = new StackClosureContext<>();

    private final String tokenId;
    private final Locale locale;
    private final Destination destination;
    private Subjoin subjoin = new SubjoinWrapper.DefaultSubjoin(SUBJOIN_CONTEXT.get());
    private DefinitionContext definitionContext;

    public ClientSideContext(Destination destination, DefinitionContext definitionContext) {
        init(definitionContext);
        this.destination = destination;
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        tokenId = serviceContext == null ?
                ClientTokenManagement.getTokenId(destination) :
                ClientTokenManagement.getTokenId(destination, serviceContext.getTokenId());
        locale = serviceContext == null ?
                Locale.getDefault() : serviceContext.getLocale();
    }

    @Override
    public String getTokenId() {
        return tokenId;
    }

    public Destination getDestination() {
        return destination;
    }

    private void init(DefinitionContext definitionContext) {
        this.definitionContext = IF.isNull(
                definitionContext,
                "Definition MUST NOT null."
        );
    }

    public DefinitionContext getDefinitionContext() {
        return definitionContext;
    }

    @Override
    public Subjoin getSubjoin() {
        return subjoin;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    public void responseSubjoin(Subjoin subjoin) {
        this.subjoin = subjoin;
    }
}
