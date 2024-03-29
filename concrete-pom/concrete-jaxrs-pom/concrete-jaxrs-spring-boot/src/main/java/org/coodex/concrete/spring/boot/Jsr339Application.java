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

package org.coodex.concrete.spring.boot;

import org.coodex.concrete.support.jsr339.ConcreteJSR339Application;

public class Jsr339Application extends ConcreteJSR339Application {

//    private final static Logger log = LoggerFactory.getLogger(Jsr339Application.class);

    public Jsr339Application() {
//        register(JacksonFeature.class);
//        register(ConcreteMessageWriter.class);
//        register(ConcreteJacksonFeature.class);
        register(ConcreteJacksonJsonProvider.class);
        register(ConcreteJAXRSBeanDefinitionRegistrar.getClasses());
        registerPackage(ConcreteJAXRSBeanDefinitionRegistrar.getApiPackages());
    }

}
