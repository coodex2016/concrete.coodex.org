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

import java.lang.reflect.Type;

import static org.coodex.util.Common.randomChar;

public class MobilePhoneNumTypeMocker extends AbstractTypeMocker<MobilePhoneNum> {
    @Override
    protected Class<?>[] getSupportedClasses() {
        return new Class<?>[]{String.class};
    }

    @Override
    protected boolean accept(MobilePhoneNum annotation) {
        return annotation != null;
    }

    @Override
    public Object mock(MobilePhoneNum mockAnnotation, Type targetType) {
        StringBuilder builder = new StringBuilder().append(1).append(randomChar("3578"));

        for (int i = 3; i <= 11; i++) {
            builder.append(randomChar("0123456789"));
            if (mockAnnotation.appleStyle() && (i == 3 || i == 7)) {
                builder.append('-');
            }
        }

        return builder.toString();
    }
}
