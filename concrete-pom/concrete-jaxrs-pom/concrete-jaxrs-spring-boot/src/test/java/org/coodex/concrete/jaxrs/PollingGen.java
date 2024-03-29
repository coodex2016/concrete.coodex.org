/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.own.OwnServiceUnit;
import org.coodex.concrete.protobuf.ProtobufModule;

import java.util.Arrays;

public class PollingGen {
    public static void main(String[] args) {
        ProtobufModule m = new ProtobufModule(Polling.class, "jaxrs");
        Arrays.stream(m.getUnits()).forEach(uint -> System.out.println(OwnServiceUnit.getUnitKey(uint)));
    }
}
