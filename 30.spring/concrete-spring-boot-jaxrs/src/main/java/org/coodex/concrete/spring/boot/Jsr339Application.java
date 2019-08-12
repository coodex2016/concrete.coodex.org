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
import org.coodex.config.Config;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.getApiPackages;

public class Jsr339Application extends ConcreteJSR339Application {

    private final static Logger log = LoggerFactory.getLogger(Jsr339Application.class);

    public Jsr339Application() {
        register(JacksonFeature.class);
        register(toRegistered().toArray(new Class[0]));
        registerPackage(toRegisteredPackages().toArray(new String[0]));
    }

    protected Set<Class> toRegistered() {
        Set<Class> classes = new HashSet<>();
        for (String str : Config.getArray("concrete.jaxrs.classes", ",", new String[0], "concrete", "jaxrs")) {
            try {
                classes.add(Class.forName(str));
            } catch (ClassNotFoundException e) {
                log.info("registered failed. class not found: {}", str);
            }
        }
        return classes;
    }

    protected Set<String> toRegisteredPackages() {
        return new HashSet<>(Arrays.asList(getApiPackages(getNamespace())));
    }

}
