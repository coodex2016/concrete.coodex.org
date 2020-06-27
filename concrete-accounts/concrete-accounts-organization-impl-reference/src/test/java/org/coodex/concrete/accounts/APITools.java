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

package org.coodex.concrete.accounts;

import org.coodex.concrete.accounts.organization.reference.api.PersonService;
import org.coodex.concrete.apitools.API;
import org.coodex.concrete.apitools.jaxrs.angular.AngularCodeRenderer;
import org.coodex.concrete.apitools.jaxrs.service.ServiceDocRenderer;

import java.io.IOException;

/**
 * Created by davidoff shen on 2017-05-02.
 */
public class APITools {

    public static void main(String[] args) throws IOException {

        API.generate(ServiceDocRenderer.RENDER_NAME, "/concrete/accounts/restful.doc",
                PersonService.class.getPackage().getName());

        API.generate(AngularCodeRenderer.RENDER_NAME + ".accounts/organization", "D:\\Projects\\front_ends\\ng2-admin\\src",
                PersonService.class.getPackage().getName());
    }
}
