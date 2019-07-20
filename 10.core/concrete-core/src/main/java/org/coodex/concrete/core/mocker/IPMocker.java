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

import org.coodex.concrete.api.mockers.IP;
import org.coodex.pojomocker.AbstractMocker;
import org.coodex.util.Common;

@Deprecated
public class IPMocker extends AbstractMocker<IP> {
    @Override
    public Object mock(IP mockAnnotation, Class clazz) {
        byte[] ip = new byte[4];
        for (int i = 0; i < 4; i++) {
            ip[i] = (byte) Common.random(Integer.MAX_VALUE);
        }
        if (String.class.equals(clazz)) {
            return String.format("%d.%d.%d.%d",
                    ip[0] & 0xFF, ip[1] & 0xFF, ip[2] & 0xFF,
                    ip[3] & 0xFF);
        } else if (clazz.isArray() && byte.class.equals(clazz.getComponentType())) {
            return ip;
        } else
            return null;
    }
}
