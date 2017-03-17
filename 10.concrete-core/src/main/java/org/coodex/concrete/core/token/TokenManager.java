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

package org.coodex.concrete.core.token;

import org.coodex.concrete.common.Token;

/**
 * Created by davidoff shen on 2016-11-02.
 */
public interface TokenManager {

    long DEFAULT_MAX_IDLE = 60; //默认60分钟
    /**
     * 获取一个已存在的令牌，令牌不存在返回空值
     *
     * @param id
     * @return
     */
    Token getToken(String id);

    /**
     * 获取一个令牌，若该id令牌不存在且force为真是，则创建一个
     *
     * @param id
     * @param force
     * @return
     */
    Token getToken(String id, boolean force);
}
