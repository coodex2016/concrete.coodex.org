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

import org.coodex.concrete.common.Token;
import org.coodex.testcase.api.TestCase;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TestCaseImpl implements TestCase {

    @Inject
    private Token token;

    @Override
    public int add(Integer x1, Integer x2) {
        token.setAttribute("key","key");
//        throw new RuntimeException("hello world.");
        return x1 + x2;
    }
}
