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

package org.coodex.concrete.apitools;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public interface ConcreteAPIRender {

    /**
     * <pre>例如：
     *   服务提供类型：JaxRS
     *   类型：code, doc
     *   使用者：backend, jquery, angularjs, angualr2, java, c#等
     *   文档化格式：gitbook, asciidoctor, markdown</pre>
     *
     * @param desc <i>服务提供类型</i>.<i>类型</i>.<i>使用者</i>.<i>文档化格式</i>.<i>版本</i>
     * @return
     */
    boolean isAccept(String desc);

    /**
     * @param packages 检索的包
     */
    void writeTo(String... packages) throws IOException;


    void setRoot(String rootPath);

}
