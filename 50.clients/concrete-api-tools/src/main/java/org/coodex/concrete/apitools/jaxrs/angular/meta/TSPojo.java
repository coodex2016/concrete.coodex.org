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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2017-04-11.
 */
public class TSPojo extends TSClass {

    private List<TSField> fields = new ArrayList<TSField>();
    private String superClass;
    private Class superType;
    public TSPojo(Class c) {
        super(CLASS_TYPE_POJO, c);
        this.superType = c.getSuperclass();
    }

    public List<TSField> getFields() {
        return fields;
    }

    public void setFields(List<TSField> fields) {
        this.fields = fields;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public Class getSuperType() {
        return superType;
    }

}
