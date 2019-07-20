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

package org.coodex.concrete.core.mocker;

import org.coodex.concrete.api.mockers.MAC;
import org.coodex.pojomocker.AbstractMocker;
import org.coodex.util.Common;

@Deprecated
public class MACMocker extends AbstractMocker<MAC> {
    @Override
    public Object mock(MAC mockAnnotation, Class clazz) {
        byte[] mac = new byte[6];
        for (int i = 0; i < 6; i++) {
            mac[i] = (byte) Common.random(Integer.MAX_VALUE);
        }
        if (String.class.equals(clazz)) {
            return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                    mac[0] & 0xFF, mac[1] & 0xFF, mac[2] & 0xFF,
                    mac[3] & 0xFF, mac[4] & 0xFF, mac[5] & 0xFF);
        } else if (clazz.isArray() && byte.class.equals(clazz.getComponentType())) {
            return mac;
        } else
            return null;
    }
}
