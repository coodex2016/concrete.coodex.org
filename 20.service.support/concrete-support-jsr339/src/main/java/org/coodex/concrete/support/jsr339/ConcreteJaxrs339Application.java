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

package org.coodex.concrete.support.jsr339;

import org.coodex.concrete.jaxrs.ClassGenerator;
import org.coodex.concrete.jaxrs.ConcreteJaxrsApplication;
import org.coodex.concrete.support.jsr339.javassist.JSR339ClassGenerator;

import javax.ws.rs.core.Application;
import java.util.Map;

public class ConcreteJaxrs339Application extends ConcreteJaxrsApplication {

    public ConcreteJaxrs339Application() {
        super();
    }

    @Override
    protected ClassGenerator getClassGenerator() {
        return classGenerator;
    }

    public ConcreteJaxrs339Application(Application application) {
        super(application);
    }


    private static JSR339ClassGenerator classGenerator = new JSR339ClassGenerator();


    @Override
    public Map<String, Object> getProperties() {
        return application != null ? application.getProperties() : super.getProperties();
    }
}
