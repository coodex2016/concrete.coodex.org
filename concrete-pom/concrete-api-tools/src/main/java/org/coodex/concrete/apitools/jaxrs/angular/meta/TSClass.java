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

package org.coodex.concrete.apitools.jaxrs.angular.meta;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-04-11.
 */
public abstract class TSClass {
    protected static final int CLASS_TYPE_MODULE = 0;
    protected static final int CLASS_TYPE_POJO = 1;

    private final int classType;
    private final String packageName;
    private final String className;
    private List<String> genericParams = new ArrayList<String>();
    private Set<Class> imports = new HashSet<Class>();

    public TSClass(int classType, Class clz) {
        this.classType = classType;
        this.packageName = clz.getPackage().getName();
        this.className = clz.getSimpleName();
        for (TypeVariable t : clz.getTypeParameters()) {
            genericParams.add(t.getName());
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public Set<Class> getImports() {
        return imports;
    }

    public void setImports(Set<Class> imports) {
        this.imports = imports;
    }

    public String getClassName() {
        return className;
    }

    public int getClassType() {
        return classType;
    }

    public List<String> getGenericParams() {
        return genericParams;
    }

    public void setGenericParams(List<String> genericParams) {
        this.genericParams = genericParams;
    }
}
