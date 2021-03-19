/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

public final class SPI {

    /**
     * 装饰高于继承
     *
     * @param o 服务实例
     * @return 服务顺序
     */
    public static int getServiceOrder(Object o) {
        return Optional.ofNullable(o)
                .map(service -> service.getClass().getAnnotation(Ordered.class))
                .map(Ordered::value)
                .orElse(o instanceof Sequential ? ((Sequential) o).order() : Integer.MAX_VALUE);

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Ordered {
        int value() default Integer.MAX_VALUE;
    }

    public interface Sequential {
        int order();
    }

}
