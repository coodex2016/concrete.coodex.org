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

package org.coodex.servlet.cors;

/**
 * 基于www.w3.org/TR/cors设计的CORS参数设定，定义了5.1至5.6的6个响应头。版本：20140116
 *
 * @author davidoff
 */
public interface CORSSetting {

    String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    String EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    String MAX_AGE = "Access-Control-Max-Age";
    String ALLOW_METHOD = "Access-Control-Allow-Methods";
    String ALLOW_HEADERS = "Access-Control-Allow-Headers";

    String KEY_ALLOW_ORIGIN = "allowOrigin";
    String KEY_EXPOSE_HEADERS = "exposeHeaders";
    String KEY_ALLOW_METHOD = "allowMethod";
    String KEY_ALLOW_HEADERS = "allowHeaders";
    String KEY_MAX_AGE = "maxAge";
    String KEY_ALLOW_CREDENTIALS = "allowCredentials";

    /**
     * @return 空格或逗号分隔，为null表示不需要设置
     */
    String allowOrigin();

    /**
     * @return 依RFC 2616规范，使用逗号分隔，为null表示不需要设置
     */
    String exposeHeaders();

    /**
     * @return 依RFC 2616规范，使用逗号分隔，为null表示不需要设置
     */
    String allowMethod();

    /**
     * @return 依RFC 2616规范，使用逗号分隔，为null表示不需要设置
     */
    String allowHeaders();

    /**
     * @return 为null表示不需要设置
     */
    Long maxAge();

    /**
     * @return 为null表示不需要设置
     */
    Boolean allowCredentials();

}
