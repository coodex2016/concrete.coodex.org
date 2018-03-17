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

package org.coodex.concrete.jaxrs.struct;

import org.coodex.concrete.common.struct.AbstractParam;

import java.lang.reflect.Method;


/**
 * Created by davidoff shen on 2016-11-30.
 */
public class Param extends AbstractParam {


//    private final Description description;
    private boolean pathParam = true;

    public Param(Method method, int index) {
        super(method, index);
    }

    public boolean isPathParam() {
        return pathParam;
    }

    public void setPathParam(boolean pathParam) {
        this.pathParam = pathParam;
    }
}
