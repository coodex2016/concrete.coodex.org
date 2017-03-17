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

package org.coodex.concrete.common;

import org.coodex.util.Profile;
import org.coodex.util.SPIFacade;

/**
 * 使用concrete.properties解决冲突的SPIFacade。
 * <p>
 * 某个接口有多个services配置时，getInstance的时候会产生冲突，ConcreteSPIFacade使用concrete.properties解决冲突。
 * <pre>
 *     <i>interfaceClass</i>.provider = <i>service Class</i>
 * </pre>
 *
 * Created by davidoff shen on 2016-09-08.
 */
public abstract class ConcreteSPIFacade<T> extends SPIFacade<T> {


    private static Profile profile = ConcreteToolkit.getProfile();


    protected ConcreteSPIFacade() {
        super();
    }

    @Override
    protected T conflict() {
        String key = profile.getString(getInterfaceClass().getCanonicalName() + ".provider");
        return instances.containsKey(key) ? instances.get(key) : super.conflict();
    }

}

