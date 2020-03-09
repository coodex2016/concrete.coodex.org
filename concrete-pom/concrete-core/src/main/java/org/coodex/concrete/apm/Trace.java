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

package org.coodex.concrete.apm;

import org.coodex.concrete.common.Subjoin;

public interface Trace {

//    enum Kind{
//        CLIENT, SERVER
//    }
//
//    Trace kind(Kind kind);

    Trace start();

    Trace start(String name);

    Trace type(String type);

    Trace tag(String name, String value);

//    void appendTo(Trace trace);

    void error(Throwable throwable);

    void finish();

    void hack(Subjoin subjoin);
}
