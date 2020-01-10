/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.spring.AbstractRuntimeParameter;
import org.springframework.core.annotation.AnnotationAttributes;

public class DubboRuntime extends AbstractRuntimeParameter {
    public DubboRuntime() {
        this(null, null);
    }

    public DubboRuntime(String[] apiPackages, Class[] classes) {
        super(apiPackages, classes);
    }

    private String name;
    private String[] protocols;
    private String[] registries;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getProtocols() {
        return protocols;
    }

    public void setProtocols(String[] protocols) {
        this.protocols = protocols;
    }

    public String[] getRegistries() {
        return registries;
    }

    public void setRegistries(String[] registries) {
        this.registries = registries;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    protected String getNamespace() {
        return "dubbo";
    }

    @Override
    protected void loadCustomRuntimeConfigFrom(AnnotationAttributes annotationAttributes) {
        this.protocols = annotationAttributes.getStringArray("protocols");
        this.name = annotationAttributes.getString("applicationName");
        this.registries = annotationAttributes.getStringArray("registries");
        this.version = annotationAttributes.getString("version");
    }
}
