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

package org.coodex.concrete.common;

public class ThrowableMapperFacade {

//    @Deprecated
//    private static LazySelectableServiceLoader<Throwable, ThrowableMapper> mapperLoader
//            = new LazySelectableServiceLoader<Throwable, ThrowableMapper>() {
//    };


    public static ErrorInfo toErrorInfo(Throwable exception) {

//        ConcreteException concreteException = ConcreteHelper.findException(exception);
        ConcreteException concreteException = ConcreteHelper.getException(exception);

//        if (concreteException != null) {
        return new ErrorInfo(concreteException.getCode(), concreteException.getMessage());
//        } else {
//            ThrowableMapper mapper = mapperLoader.select(exception);
//            if (mapper != null) {
//                return mapper.toErrorInfo(exception);
//            } else {
//                return new ErrorInfo(ErrorCodes.UNKNOWN_ERROR, exception.getLocalizedMessage());
//            }
//        }
    }
}
