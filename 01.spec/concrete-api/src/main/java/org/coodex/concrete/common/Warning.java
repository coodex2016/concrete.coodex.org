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

package org.coodex.concrete.common;

import java.io.Serializable;

/**
 * 当Concrete服务可以正常执行，但是需要额外通知调用者警告信息(例如，服务快到期了，服务端资源快满了等)时，
 * 可以通过Subjoin随结果数据通知到调用者
 */
public interface Warning extends Serializable {
    /**
     * @return 警告代码
     */
    Integer getCode();

    /**
     * @return 警告文本信息，同ErrorMessage体系
     */
    String getMessage();

}
