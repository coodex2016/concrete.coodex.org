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

import org.coodex.concrete.api.mockers.EMail;
import org.coodex.pojomocker.AbstractMocker;

import static org.coodex.util.Common.random;
import static org.coodex.util.Common.randomStr;

/**
 * Created by davidoff shen on 2017-05-16.
 */
@Deprecated
public class EMailMocker extends AbstractMocker<EMail> {
    private final String[] top = {".com", ".net", ".org", ".gov", ".cn"};
    private final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    public Object mock(EMail mockAnnotation, Class clazz) {
        String domain = mockAnnotation.domains().length > 0 ?
                random(mockAnnotation.domains()) :
                randomDomain();
        return randomStr(3, 9, chars) + '@' + domain;
    }

    private String randomDomain() {
        return randomStr(5, 10, chars) + random(top);
    }
}
