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

package org.coodex.closure;


/**
 * Created by davidoff shen on 2016-09-04.
 */
public abstract class AbstractClosureContext<VariantType> {


    private final ThreadLocal<VariantType> threadLocal = new ThreadLocal<VariantType>();

    protected final VariantType $getVariant() {
        return threadLocal.get();
    }

//    @Deprecated
//    protected final Object closureRun(VariantType variant, Closure runnable) {
//        if (runnable == null) return null;
//        threadLocal.set(variant);
//        try {
//            return runnable.run();
//        } finally {
//            threadLocal.remove();
//        }
//    }

    protected final Object closureRun(VariantType variant, CallableClosure callable) throws Throwable {
        if (callable == null) return null;
        threadLocal.set(variant);
        try {
            return callable.call();
        } finally {
            threadLocal.remove();
        }
    }

}
