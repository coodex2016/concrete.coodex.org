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

package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;
import org.coodex.util.Parameter;

/**
 * Created by davidoff shen on 2017-02-09.
 */
@MicroService("Calc")
public interface Calc extends ConcreteService {

    @Description(name = "订阅演示")
    void subscribe();

    @Description(name = "求和", description = "求X + Y = ")
    int add(@Description(name = "被加数", description = "被加数")
            @Parameter("x") int x,
            @Description(name = "加数", description = "加数")
            @Parameter("y") int y);
}
