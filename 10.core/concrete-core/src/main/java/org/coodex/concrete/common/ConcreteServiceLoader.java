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

package org.coodex.concrete.common;

import org.coodex.config.Config;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;

import java.util.Collection;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * 使用concrete.properties解决冲突的SPIFacade。
 * <p>
 * 某个接口有多个services配置时，getInstance的时候会产生冲突，ConcreteSPIFacade使用concrete.properties解决冲突。
 * <pre>
 *     <i>interfaceClass</i>.provider = <i>service Class</i>
 * </pre>
 * <p>
 * Created by davidoff shen on 2016-09-08.
 */
@Deprecated
public abstract class ConcreteServiceLoader<T> extends ServiceLoaderImpl<T> {

    private ServiceLoader<T> serviceLoaderFacade = new ServiceLoaderImpl<T>() {
    };

    private boolean init = false;

    protected ConcreteServiceLoader() {
        super();
    }

//    @Override
//    protected void loadInstances() {
//        synchronized (this) {
//            super.loadInstances();
//            if (!init) {
//                try {
//                    Map<String, T> instances = super.getInstances();
//                    Map<String, T> beans = BeanServiceLoaderProvider.getBeanProvider().getBeansOfType(getInterfaceClass());
//                    if (beans != null && beans.size() > 0) {
//                        for (String key : beans.keySet()) {
//                            T t = beans.get(key);
//                            if (t != null && !instances.values().contains(t)) {
//                                instances.put(key, t);
//                            }
//                        }
//                    }
//                } catch (Throwable th) {
//                    // ？？？
//                }
//                init = true;
//            }
//        }
//    }

    @Override
    protected T conflict(Class<? extends T> providerClass, Map<String, T> map) {
        String key = Config.get(providerClass + ".provider", getAppSet());
        return map.containsKey(key) ? map.get(key) : super.conflict(providerClass, map);
    }

    @Override
    protected T conflict() {
        String key = Config.get(getInterfaceClass().getCanonicalName() + ".provider", getAppSet());
        Map<String, T> instances = serviceLoaderFacade.getInstances();
        return instances.containsKey(key) ? instances.get(key) : super.conflict();
    }

    private T getDefaultProviderFromConfiguration() {
        String key = Config.get(getInterfaceClass().getCanonicalName() + ".default", getAppSet());
        Map<String, T> instances = serviceLoaderFacade.getInstances();
        return instances.getOrDefault(key, null);
    }

    protected T getConcreteDefaultProvider() {
        return super.getDefaultProvider();
    }

    @Override
    public final T getDefaultProvider() {
        T instance = getDefaultProviderFromConfiguration();
        return instance == null ? getConcreteDefaultProvider() : instance;
    }


    @Override
    public Collection<T> getAllInstances() {
        return serviceLoaderFacade.getAllInstances();
    }

    @Override
    public Map<String, T> getInstances() {
        return serviceLoaderFacade.getInstances();
    }

    @Override
    public T getInstance(Class<? extends T> providerClass) {
        return serviceLoaderFacade.getInstance(providerClass);
    }

    @Override
    public T getInstance(String name) {
        return serviceLoaderFacade.getInstance(name);
    }

    @Override
    public T getInstance() {
        return serviceLoaderFacade.getInstance();
    }
}

