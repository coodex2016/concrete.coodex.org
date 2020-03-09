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

import org.coodex.mock.Mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Mock
public @interface IpAddress {

    enum Type{
        IPV4(4), MAC(6),TPV6(16);

        private int size;
        Type(int size){
            this.size = size;
        }

        public int getSize() {
            return size;
        }

        public String ipToString(int [] ip){
            if(ip == null)
                throw new NullPointerException("IP null.");
            if(ip.length != size){
                throw new IllegalArgumentException("size mismatch.");
            }

            char s = ip.length >= 6 ? ':' : '.';
            boolean v6 = ip.length == 16;
            String format = ip.length >= 6 ? "%02X": "%d";
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < ip.length; i++) {
                if (i > 0 && (!v6 || i % 2 == 0)) {
                    builder.append(s);
                }
                builder.append(String.format(format, ip[i]));
            }
            return builder.toString();
        }
    }

    Type type() default Type.IPV4;

}
