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

package org.coodex.testcase.impl;

import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.Warning;
import org.coodex.testcase.api.TestCase2;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;

@Named
public class TestCaseImpl implements TestCase2 {

    @Inject
    private Token token;

    @Inject
    private Subjoin subjoin;

    @Override
    public int add(Integer x1, Integer x2) {

        token.setAttribute("key","key");
        subjoin.set("abcd", Arrays.asList("sadfaf"));
        subjoin.putWarning(new Warning() {
            @Override
            public Integer getCode() {
                return 3333;
            }

            @Override
            public String getMessage() {
                return "safaf";
            }
        });
//        throw new RuntimeException("hello world.");
        return x1 + x2;
    }

    @Override
    public String helloWorld() {
        return "hello world.";
    }
}
