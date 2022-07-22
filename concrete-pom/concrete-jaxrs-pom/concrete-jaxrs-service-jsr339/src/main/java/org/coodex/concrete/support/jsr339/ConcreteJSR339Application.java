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

package org.coodex.concrete.support.jsr339;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.ClassGenerator;
import org.coodex.concrete.jaxrs.ConcreteJaxrsApplication;
import org.coodex.concrete.protobuf.ProtobufServiceApplication;
import org.coodex.concrete.support.jsr339.javassist.JSR339ClassGenerator;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.util.Map;

public class ConcreteJSR339Application extends ConcreteJaxrsApplication {

    private static final Logger log = LoggerFactory.getLogger(ConcreteJSR339Application.class);

    public static final String GRABLE_SERVICE_CLASS_NAME = "org.coodex.concrete.jaxrs.garble.GrableService";
    public static final boolean JAXRS_GRABLE_SERVICE_EXISTS = ReflectHelper.classExists(
            GRABLE_SERVICE_CLASS_NAME
    );
    private final boolean grableEnabled;

    private void checkGrableServiceStatus() {
        if (grableEnabled && !JAXRS_GRABLE_SERVICE_EXISTS) {
            log.warn("class {} not found. grable service could not start, add org.coodex.concrete.jaxrs:concrete-jaxrs-grable:{} to use it.",
                    GRABLE_SERVICE_CLASS_NAME, ConcreteHelper.VERSION);
        }
    }


    public ConcreteJSR339Application() {
        this(JAXRS_GRABLE_SERVICE_EXISTS);
    }

    public ConcreteJSR339Application(boolean grableEnabled) {
        super();
        this.grableEnabled = grableEnabled;
        checkGrableServiceStatus();
    }


    public ConcreteJSR339Application(Application application) {
        this(application, JAXRS_GRABLE_SERVICE_EXISTS);
    }


    public ConcreteJSR339Application(Application application, boolean grableEnabled) {
        super(application);
        this.grableEnabled = grableEnabled;
        checkGrableServiceStatus();
    }

    private static final JSR339ClassGenerator classGenerator = new JSR339ClassGenerator();

    protected boolean isGrableEnabled() {
        return JAXRS_GRABLE_SERVICE_EXISTS && grableEnabled;
    }

    @Override
    protected void registerConcreteService(Class<?> concreteServiceClass) {
        if (isGrableEnabled()) {
            ProtobufServiceApplication.getInstance("jaxrs").register(concreteServiceClass);
        }
        super.registerConcreteService(concreteServiceClass);
    }

    @Override
    protected ClassGenerator getClassGenerator() {
        return classGenerator;
    }

    @Override
    public Map<String, Object> getProperties() {
        return getApplication() != null ? getApplication().getProperties() : super.getProperties();
    }
}
