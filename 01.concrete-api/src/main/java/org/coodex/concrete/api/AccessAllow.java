/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 业务接口访问权限定义，与BizUnit一起使用
 * <p>
 * Created by davidoff shen on 2016-09-01.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessAllow {

    /**
     * 所有人都能访问
     */
    String EVERYBODY = "EVERYBODY";

    String PREROGATIVE = "*";

    /**
     * 访问业务所需的角色
     * 如置空，则表明只要是有效用户即可访问
     *
     * @return
     */
    String[] roles() default {EVERYBODY};
}
