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

package org.coodex.pojomocker.mockers;

import org.coodex.pojomocker.AbstractPrimitiveMocker;
import org.coodex.pojomocker.annotations.CHAR;
import org.coodex.util.Common;
import org.coodex.util.Profile;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class DefaultCharMocker extends AbstractPrimitiveMocker<Character, CHAR> {

    static final String DEFAULT_RANGE_STR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:;{}[]`!@#$%^&*()_+-='\",./<>?|\\";

    static char[] getDefaultRange() {
        return Profile.getProfile("mock.properties").getString("default.chars.range", DEFAULT_RANGE_STR).toCharArray();
    }


    @Override
    protected Object toPrimitive(Character character) {
        return character == null ? (char) 0 : character.charValue();
    }

    @Override
    protected Character $mock(CHAR mockAnnotation) {
        char[] range = mockAnnotation.range();
        if (range == null || range.length == 0) {
            range = getDefaultRange();
        }

        return Character.valueOf(range[Common.random(range.length - 1)]);
    }
}
