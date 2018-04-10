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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.AbstractErrorCodes;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.util.ReflectHelper;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.foreachClassInPackages;

public abstract class ConcreteJaxrsApplication extends Application implements org.coodex.concrete.api.Application {

    protected Set<Class<? extends ConcreteService>> servicesClasses = new HashSet<Class<? extends ConcreteService>>();
    protected Set<Class<?>> jaxrsClasses = new HashSet<Class<?>>();
    protected Set<Object> singletonInstances = new HashSet<Object>();
    protected Set<Class<?>> othersClasses = new HashSet<Class<?>>();

    public ConcreteJaxrsApplication() {
        super();
        registerDefault();
    }

    protected Application application = null;

    protected static final JaxRSModuleMaker moduleMaker = new JaxRSModuleMaker();

    protected abstract ClassGenerator getClassGenerator();

    public ConcreteJaxrsApplication(Application application) {
        this.application = application;
        registerDefault();
    }

    protected void registerDefault() {
        register(ConcreteExceptionMapper.class, Polling.class);
        registerPackage(ErrorCodes.class.getPackage().getName());
    }

    @Override
    public void registerPackage(String... packages) {

        foreachClassInPackages(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                registerClass(serviceClass);
            }
        }, packages);
    }

    @Override
    public void register(Class<?>... classes) {
        for (Class<?> clz : classes) {
            registerClass(clz);
        }
    }

    private Object newInstance(Class<?> clz) {
        try {
            return clz.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable th) {
            throw new UnableNewInstanceException(th);
        }
    }


    protected void registerConcreteService(Class<? extends ConcreteService> concreteServiceClass) {
        if (!servicesClasses.contains(concreteServiceClass)) {
            servicesClasses.add(concreteServiceClass);
            Class<?> jaxrs = generateJaxrsClass(concreteServiceClass);
            if (jaxrs != null) {
                jaxrsClasses.add(jaxrs);
//                singletonInstances.add(newInstance(jaxrs));
            }
        }
    }

    protected Class<?> generateJaxrsClass(Class<?> concreteServiceClass){
        try {
            return getClassGenerator().generatesImplClass(moduleMaker.make(concreteServiceClass));
        }catch (RuntimeException t){
            throw t;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void registerClass(Class<?> clz) {
        if (ConcreteHelper.isConcreteService(clz)) {
            registerConcreteService((Class<? extends ConcreteService>) clz);
        } else if (AbstractErrorCodes.class.isAssignableFrom(clz)) {
            ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clz);
        } else {
            if (!othersClasses.contains(clz)) {
                othersClasses.add(clz);
                if (clz.getAnnotation(Provider.class) != null) {
                    singletonInstances.add(newInstance(clz));
                }
            }
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<Class<?>>();
        if (application != null) {
            set.addAll(application.getClasses());
        }
        set.addAll(jaxrsClasses);
        set.addAll(othersClasses);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> set = new HashSet<Object>();
        if (application != null) {
            set.addAll(application.getSingletons());
        }
        set.addAll(singletonInstances);
        return Collections.unmodifiableSet(set);
    }

    private class UnableNewInstanceException extends RuntimeException {
        public UnableNewInstanceException(Throwable th) {
            super(th);
        }
    }

}
