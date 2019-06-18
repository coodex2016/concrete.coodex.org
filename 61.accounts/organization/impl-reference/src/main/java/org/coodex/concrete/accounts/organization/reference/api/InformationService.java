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

import org.coodex.concrete.accounts.organization.api.AbstractInformationService;
import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.ConcreteService;

/**
 * Created by davidoff shen on 2017-05-02.
 */
@ConcreteService("organization")
@Description(name = "组织结构信息服务")
public interface InformationService
        extends AbstractInformationService
        <Institution, Department, Position, Person> {
}
