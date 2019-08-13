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

import org.coodex.concrete.spring.AbstractRuntimeParameter;
import org.coodex.concrete.support.jsr339.ConcreteJSR339Application;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.springframework.core.annotation.AnnotationAttributes;

public class JaxrsRuntime extends AbstractRuntimeParameter {


    private String[] urlMappings;
    private Class<? extends ConcreteJSR339Application> applicationClass;

    public JaxrsRuntime() {
        this(null, null, null, null);
    }

    public JaxrsRuntime(String[] apiPackages, String[] urlMappings,
                        Class[] classes, Class<? extends ConcreteJSR339Application> applicationClass) {
        super(apiPackages, classes);
        this.urlMappings = urlMappings;
        this.applicationClass = applicationClass;
    }


    public String[] getUrlMappings() {
        String[] thisUrlMappings = null;
        if (urlMappings == null || urlMappings.length == 0) {
            thisUrlMappings = Config.getArray("servletMapping", ",", new String[0]);
        } else {
            thisUrlMappings = urlMappings;
        }

        if (thisUrlMappings == null || thisUrlMappings.length == 0) {
            thisUrlMappings = new String[]{"/jaxrs/*"};
        }
        return thisUrlMappings;
    }

    public String getApplicationClassName() {
        return applicationClass == null ?
                Jsr339Application.class.getName() :
                applicationClass.getName();
    }

    @Override
    protected String getNamespace() {
        return "jaxrs";
    }

    @Override
    protected void loadCustomRuntimeConfigFrom(AnnotationAttributes annotationAttributes) {
        this.urlMappings = annotationAttributes.getStringArray("servletMappingUrls");
        this.applicationClass = annotationAttributes.getClass("application");
    }
}
