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

package org.coodex.concrete.api;

import java.lang.annotation.*;

/**
 * 业务描述注解，与ConcreteService一起使用
 * <p>
 * 2017-02-21 可以装饰pojo属性和parameter。pojo属性指public field和getXXX isXXX(boolean)
 * Created by davidoff shen on 2016-08-31.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Description {

    /**
     * @return 描述对象供文档化的名称
     */
    String name();

    /**
     * @return 描述对象供文档化的详细说明<s>，如果以".MD"结尾，则使用markdown文件</s>
     */
    String description() default "";
}
