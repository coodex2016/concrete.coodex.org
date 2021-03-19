/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.mock.ext;

import org.coodex.mock.AbstractTypeMocker;
import org.coodex.util.Common;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Random;

import static org.coodex.util.GenericTypeHelper.typeToClass;

public class IpAddressTypeMocker extends AbstractTypeMocker<IpAddress> {
    @Override
    protected Class<?>[] getSupportedClasses() {
        return new Class<?>[]{
                String.class,
                int[].class, Integer[].class,
                byte[].class, Byte[].class
        };
    }

    @Override
    protected boolean accept(IpAddress annotation) {
        return annotation != null;
    }

    private Object to(Class<?> c, int[] ip) {
        switch (Common.indexOf(getSupportedClasses(), c)) {
//            case 0: //String
//                char s = ip.length >= 6 ? ':' : '.';
//                boolean v6 = ip.length == 16;
//                String format = ip.length >= 6 ? "%02X" : "%d";
//                StringBuilder builder = new StringBuilder();
//                for (int i = 0; i < ip.length; i++) {
//                    if (i > 0 && (!v6 || i % 2 == 0)) {
//                        builder.append(s);
//                    }
//                    builder.append(String.format(format, ip[i]));
//                }
//                return builder.toString();
            case 1:
                return ip;
            case 2:
                return toArray(ip, Integer.class, false);
            case 3:
                return toArray(ip, byte.class, true);
            case 4:
                return toArray(ip, Byte.class, true);
        }
        return null;
    }

    private Object toArray(int[] ip, Class<?> c, boolean toByte) {
        Object result = Array.newInstance(c, ip.length);
        for (int i = 0; i < ip.length; i++) {
            byte b = (byte) ip[i];
            if (toByte) {
                Array.set(result, i, b);
            } else {
                Array.set(result, i, ip[i]);
            }
        }
        return result;
    }

    @Override
    public Object mock(IpAddress mockAnnotation, Type targetType) {
        Class<?> clazz = typeToClass(targetType);
        Random random = new Random();
        int size = mockAnnotation.type().getSize();
        int[] ip = new int[size];
        for (int i = 0; i < size; i++) {
            ip[i] = random.nextInt(0x100);
        }
        if (String.class.equals(targetType))
            return mockAnnotation.type().ipToString(ip);
        else
            return to(clazz, ip);
    }
}
