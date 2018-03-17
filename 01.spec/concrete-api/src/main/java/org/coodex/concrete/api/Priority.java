/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 服务执行优先级，需要异步环境支持，例如Servlet 3.x, JAX-RS 2.x
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Overlay
public @interface Priority {

    /**
     * 指定优先级组名
     *
     * @return
     */
    int value() default Thread.NORM_PRIORITY;
}
