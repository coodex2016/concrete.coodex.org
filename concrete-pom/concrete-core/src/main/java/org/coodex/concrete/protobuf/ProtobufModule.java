/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.protobuf;

import org.coodex.concrete.own.OwnServiceModule;

import java.lang.reflect.Method;
import java.util.List;

public class ProtobufModule extends OwnServiceModule<ProtobufUnit> {

    private final String overProtocol;
    public ProtobufModule(Class<?> interfaceClass, String overProtocol) {
        super(interfaceClass);
        this.overProtocol = overProtocol;
    }

    @Override
    protected ProtobufUnit[] toArrays(List<ProtobufUnit> protobufUnits) {
        return protobufUnits.toArray(new ProtobufUnit[0]);
    }

    @Override
    protected ProtobufUnit buildUnit(Method method) {
        return new ProtobufUnit(method, this, overProtocol);
    }
}
