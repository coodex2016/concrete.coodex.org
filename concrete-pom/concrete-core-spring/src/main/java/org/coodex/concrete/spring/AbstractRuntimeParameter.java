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

package org.coodex.concrete.spring;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public abstract class AbstractRuntimeParameter {
    private final static Logger log = LoggerFactory.getLogger(AbstractRuntimeParameter.class);
    private String[] apiPackages;
    private Class<?>[] classes;

    public AbstractRuntimeParameter(String[] apiPackages, Class<?>[] classes) {
        this.apiPackages = apiPackages;
        this.classes = classes;
    }

    public String[] getApiPackages() {
        return toRegisteredPackages(apiPackages);
    }

    private String[] toRegisteredPackages(String[] packages) {
        return (packages == null || packages.length == 0) ?
                new HashSet<>(Arrays.asList(ConcreteHelper.getApiPackages(getNamespace()))).toArray(new String[0]) :
                packages;
    }

    public Class<?>[] getClasses() {
        return toRegistered(classes);
    }

    private Class<?>[] toRegistered(Class<?>[] classes) {
        return (classes == null || classes.length == 0) ?
                toRegistered().toArray(new Class<?>[0]) :
                classes;
    }

    private Set<Class<?>> toRegistered() {
        Set<Class<?>> classes = new HashSet<>();
        for (String str : Optional.ofNullable(Config.getArray(getNamespace() + ".classes", "concrete", getNamespace()))
                .orElseGet(() -> new String[0])) {
            try {
                classes.add(Class.forName(str));
            } catch (ClassNotFoundException e) {
                log.info("registered failed. class not found: {}", str);
            }
        }
        return classes;
    }

    protected <T> T get(String key, T defaultValue) {
        String s = Config.get(getNamespace() + "." + key, "concrete", getAppSet());
        return s == null ? defaultValue : Common.to(s, defaultValue);
    }


    protected abstract String getNamespace();

    protected abstract void loadCustomRuntimeConfigFrom(AnnotationAttributes annotationAttributes);

    public final void loadFrom(AnnotationAttributes annotationAttributes) {
        this.apiPackages = annotationAttributes.getStringArray("servicePackages");
        this.classes = annotationAttributes.getClassArray("classes");
        loadCustomRuntimeConfigFrom(annotationAttributes);
    }

}
