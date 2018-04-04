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

package org.coodex.concrete.accounts.organization.reference.api;

import org.coodex.concrete.accounts.organization.api.AbstractPositionManagementService;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;

/**
 * Created by davidoff shen on 2017-05-02.
 */
@MicroService("organization")
@Description(name = "职位管理", description = "职位管理的参考实现")
public interface PositionService extends AbstractPositionManagementService<Position> {
}
