/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring.boot;

import org.coodex.util.json.Jackson2JSONSerializer;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

@Deprecated
public class ConcreteJacksonFeature /* extends JacksonFeature */ implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
//        context.register(ConcreteJacksonJsonProvider.class);
//        return super.configure(context);
        context.register(new JacksonJsonProvider(Jackson2JSONSerializer.getMapper()), Integer.MAX_VALUE);
        return true;
    }
}
