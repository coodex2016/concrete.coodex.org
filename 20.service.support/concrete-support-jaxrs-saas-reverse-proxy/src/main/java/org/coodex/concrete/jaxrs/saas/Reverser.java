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

package org.coodex.concrete.jaxrs.saas;

import org.coodex.util.AcceptableService;

/**
 * 根据路由属性获得concrete服务的set的根路径;
 * <p>
 * 服务选择：根据 {@link RouteBy} 选择具体的Reverser
 * <p>
 * Created by davidoff shen on 2017-03-22.
 */
public interface Reverser extends AcceptableService<String> {

    /**
     * 根据routeBy获得concrete服务set根路径，返回值应该是set的内部地址
     *
     * @param routeBy
     * @return
     */
    String resolve(String routeBy);
}
