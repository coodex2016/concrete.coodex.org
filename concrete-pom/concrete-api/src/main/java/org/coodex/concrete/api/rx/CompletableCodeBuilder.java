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

package org.coodex.concrete.api.rx;

import java.util.concurrent.CompletableFuture;

public class CompletableCodeBuilder implements RxCodeBuilder {
    private static final String TYPE_NAME = "Completable";

    @Override
    public String getReturnTypeCode(String syncReturnType) {
        return CompletableFuture.class.getName() + "<" + syncReturnType + ">";
    }

    @Override
    public String getAdj() {
        return TYPE_NAME;
    }

}
