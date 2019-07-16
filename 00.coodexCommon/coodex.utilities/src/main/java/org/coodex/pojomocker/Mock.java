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

package org.coodex.pojomocker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by davidoff shen on 2017-05-11.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Mock {

    String POLICY_KEY = Mock.class.getName() + ".policy";

//
//
//    enum Policy {
//        DEFAULT, WARN, FAIL
//    }
//
////    Class<? extends Mocker> mocker();
//
//    /**
//     * 模拟过程出现问题时如何处理，默认DEFAULT
//     * <ol>
//     * <li>DEFAULT：使用系统环境变量org.coodex.pojomocker.Mock.policy设定，warn同WARN，其他同FAIL</li>
//     * <li>WARN: 使用log输出警告信息</li>
//     * <li>FAIL: 抛出异常</li>
//     * </ol>
//     *
//     * @return
//     */
//    Policy onError() default Policy.DEFAULT;

}
