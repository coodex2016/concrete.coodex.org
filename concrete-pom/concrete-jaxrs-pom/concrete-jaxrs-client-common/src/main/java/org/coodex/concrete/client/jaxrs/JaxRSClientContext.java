/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.client.jaxrs;

import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.DefinitionContext;

import static org.coodex.concrete.common.ConcreteHelper.VERSION;
import static org.coodex.concrete.jaxrs.JaxRSHelper.KEY_CLIENT_PROVIDER;

public class JaxRSClientContext extends ClientSideContext {

    public JaxRSClientContext(Destination destination, DefinitionContext context, String agent) {
        super(destination, context);
        getSubjoin().add(KEY_CLIENT_PROVIDER, agent + " " + VERSION);
    }

}
