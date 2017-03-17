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

package org.coodex.concrete.common;

import java.io.Serializable;
import java.util.Set;

/**
 * 代表系统的一个账户
 * Created by davidoff shen on 2016-09-01.
 */
public interface Account<ID extends Serializable> {
    /**
     * 帐号ID
     *
     * @return
     */
    ID getId();

    /**
     * 帐号所拥有的角色
     *
     * @return
     */
    Set<String> getRoles();

    /**
     * 是否有效
     *
     * @return
     */
    boolean isValid();

//    /**
//     * 是否可信
//     *
//     * @return
//     */
//    boolean isCredibled();


}
