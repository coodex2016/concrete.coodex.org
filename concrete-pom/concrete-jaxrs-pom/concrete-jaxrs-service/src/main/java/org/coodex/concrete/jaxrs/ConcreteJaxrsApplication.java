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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.concrete.jaxrs.logging.ServerLogger;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.foreachClassInPackages;
import static org.coodex.util.Common.cast;

@SuppressWarnings("unused")
public abstract class ConcreteJaxrsApplication
        extends Application
        implements
        org.coodex.concrete.api.Application {

    private static final JaxRSModuleMaker moduleMaker = new JaxRSModuleMaker();
    private final static Logger log = LoggerFactory.getLogger(ConcreteJaxrsApplication.class);
    private final Set<Class<?>> servicesClasses = new HashSet<>();
    private final Set<Class<?>> jaxrsClasses = new HashSet<>();
    private final Set<Object> singletonInstances = new HashSet<>();
    private final Set<Class<?>> othersClasses = new HashSet<>();

    private String name;

    private Application application = null;
    //    private Configurable<?> configurable = null;
    private boolean exceptionMapperRegistered = false;

    private final ServiceLoader<ServiceRegisteredListener> registerNotifyServiceServiceLoader
            = new LazyServiceLoader<ServiceRegisteredListener>((instance, concreteService) -> {
    }) {
    };

    private final ServiceLoader<DefaultJaxrsClassGetter> getterServiceLoader
            = new LazyServiceLoader<DefaultJaxrsClassGetter>() {
    };

    public ConcreteJaxrsApplication() {
        super();
        registerDefault();
    }

    public ConcreteJaxrsApplication(String name) {
        this();
        this.name = name;
    }

    public ConcreteJaxrsApplication(Application application) {
        this.application = application;
        registerDefault();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Class<?>> getServicesClasses() {
        return servicesClasses;
    }

    public Set<Class<?>> getJaxrsClasses() {
        return jaxrsClasses;
    }

    public Set<Object> getSingletonInstances() {
        return singletonInstances;
    }

    public Set<Class<?>> getOthersClasses() {
        return othersClasses;
    }

    public Application getApplication() {
        return application;
    }

    protected abstract ClassGenerator getClassGenerator();

    private void registerDefault() {
        register(ServerLogger.class);
        register(Polling.class);
        registerPackage(ErrorCodes.class.getPackage().getName());
        for (DefaultJaxrsClassGetter getter : getterServiceLoader.getAll().values()) {
            register(getter.getClasses());
        }
    }

    @Override
    public void registerPackage(String... packages) {

        foreachClassInPackages(
                this::registerClass,
                packages);
    }

    @Override
    public void register(Class<?>... classes) {
        if (classes == null || classes.length == 0) return;
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

    private void notifyToAll(Class<?> concreteServiceClass) {
        for (ServiceRegisteredListener notifyService : registerNotifyServiceServiceLoader.getAll().values()) {
            try {
                notifyService.register(this, concreteServiceClass);
            } catch (Throwable th) {
                log.warn("{} notify {} failed.", concreteServiceClass.getName(), notifyService, th);
            }
        }
    }

    private void registerConcreteService(Class<?> concreteServiceClass) {
        if (!servicesClasses.contains(concreteServiceClass)) {
            notifyToAll(concreteServiceClass);
            servicesClasses.add(concreteServiceClass);
            Class<?> jaxrs = generateJaxrsClass(concreteServiceClass);

            if (jaxrs != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("\n\tclassName: ").append(jaxrs.getName()).append(";");
                    for (Method method : jaxrs.getMethods()) {
                        if (Object.class.equals(method.getDeclaringClass())) continue;
                        builder.append("\n\t\tmethod: ").append(method.getName()).append("(");
                        for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
                            if (i > 0) builder.append(", ");
                            if (method.getParameterAnnotations()[i] != null)
                                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                                    builder.append(annotation.annotationType().getName())
                                            .append(" ");
                                }
                            builder.append(method.getParameterTypes()[i].toString());
                        }
                        builder.append(");");
                    }
                    log.debug("class info:{}", builder.toString());
                }
                jaxrsClasses.add(jaxrs);
            }
        }
    }

    private Class<?> generateJaxrsClass(Class<?> concreteServiceClass) {
        try {
            return getClassGenerator().generatesImplClass(moduleMaker.make(concreteServiceClass));
        } catch (RuntimeException t) {
            throw t;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void registerClass(Class<?> clz) {
        ErrorMessageFacade.register(clz);
        if (ConcreteHelper.isConcreteService(clz)) {
            registerConcreteService(clz);
        } else {
            if (ConcreteExceptionMapper.class.isAssignableFrom(clz)) {
                exceptionMapperRegistered = true;
            }
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
        if (!exceptionMapperRegistered) {
            register(ConcreteExceptionMapper.class);
        }
        Set<Class<?>> set = new HashSet<>();
        if (application != null) {
            set.addAll(application.getClasses());
        }
        set.addAll(jaxrsClasses);
        set.addAll(othersClasses);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Object> getSingletons() {
        if (!exceptionMapperRegistered) {
            register(ConcreteExceptionMapper.class);
        }
        Set<Object> set = new HashSet<>();
        if (application != null) {
            set.addAll(application.getSingletons());
        }
        set.addAll(singletonInstances);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public String getNamespace() {
        return "jaxrs";
    }

    private static class UnableNewInstanceException extends RuntimeException {
        UnableNewInstanceException(Throwable th) {
            super(th);
        }
    }
}
