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

import org.coodex.concrete.accounts.organization.api.AbstractLoginService;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;

/**
 * Created by davidoff shen on 2017-05-03.
 */
@ConcreteService("organization")
@Description(name = "登录服务")
public interface LoginService extends AbstractLoginService {
}
