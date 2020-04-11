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

import static org.coodex.util.Common.random;
import static org.coodex.util.Common.randomStr;

public class EMailTypeMocker extends AbstractTypeMocker<EMail> {

    private final String[] top = {".com", ".net", ".org", ".gov", ".cn"};
    private final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    protected Class<?>[] getSupportedClasses() {
        return new Class<?>[]{String.class};
    }

    @Override
    protected boolean accept(EMail annotation) {
        return annotation != null;
    }

    private String randomDomain() {
        return randomStr(5, 10, chars) + random(top);
    }

    @Override
    public Object mock(EMail mockAnnotation, Type targetType) {
        String domain = mockAnnotation.domains().length > 0 ?
                random(mockAnnotation.domains()) :
                randomDomain();
        return randomStr(3, 9, chars) + '@' + domain;
    }
}
