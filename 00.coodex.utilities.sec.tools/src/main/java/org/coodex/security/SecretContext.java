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

package org.coodex.security;

import java.security.PublicKey;

/**
 * 安全上下文，负责管理密钥对
 * Created by davidoff shen on 2017-02-05.
 */
public interface SecretContext {

    /**
     *
     *
     * @return 获取公钥
     */
    PublicKey getPublicKey();


    /**
     * 重置密钥对
     */
    void reset();

    /**
     *
     * @return 当前密钥对年纪
     */
    long keyAge();

    /**
     *
     *
     * @return 用私钥解密
     */
    byte[] decrypt(byte[] cipherContent);

    /**
     *
     *
     * @param content
     * @return 公钥加密
     */
    byte[] encrypt(byte[] content);

}
