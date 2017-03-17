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

package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class AcceptableServiceSPIFacade<Param_Type, T extends AcceptableService<Param_Type>> extends SPIFacade<T> {

    private final static Logger log = LoggerFactory.getLogger(AcceptableServiceSPIFacade.class);


    public T getServiceInstance(Param_Type param) {
        for (T instance : getAllInstances()) {
            if (instance.accept(param))
                return instance;
        }
        T instance = getDefaultProvider();
        if (instance.accept(param))
            return instance;

        log.warn("no service instance accept this: {}", param);

        return null;
    }
}
