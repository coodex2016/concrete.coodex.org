package org.coodex.concrete.common;

import org.coodex.concrete.api.ConcreteService;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-09-09.
 */
public class ServiceDefinition {


    private Class<? extends ConcreteService> serviceClass;

    private Set<Method> methods = new HashSet<Method>();

    ServiceDefinition(Class<? extends ConcreteService> serviceClass, Collection<Method> methods) {
        this.serviceClass = serviceClass;
        this.methods.addAll(methods);
    }

    public Class<? extends ConcreteService> getServiceClass() {
        return serviceClass;
    }

    public Set<Method> getMethods() {
        return methods;
    }
}
