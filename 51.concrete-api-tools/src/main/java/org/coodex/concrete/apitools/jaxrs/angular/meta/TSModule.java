/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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
public class TSModule extends TSClass {

    public TSModule(Class c) {
        super(CLASS_TYPE_MODULE, c);
    }

    private String belong;
    private List<TSMethod> methods = new ArrayList<TSMethod>();


    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong;
    }

    public List<TSMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<TSMethod> methods) {
        this.methods = methods;
    }

}
