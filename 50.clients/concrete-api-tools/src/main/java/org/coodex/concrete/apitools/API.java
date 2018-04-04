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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.util.ServiceLoader;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-12-01.
 */
public class API {
    private static final ServiceLoader<ConcreteAPIRender> RENDERS =
            new ConcreteServiceLoader<ConcreteAPIRender>() {
            };

    public static void generate(String desc, String path, String... packages) throws IOException {
        if (packages == null) {
            packages = ConcreteHelper.getApiPackages();
        }
        if (RENDERS.getAllInstances().size() == 0)
            throw new RuntimeException("NONE render found.");
        for (ConcreteAPIRender render : RENDERS.getAllInstances()) {
            synchronized (render) {
                if (render.isAccept(desc)) {
                    render.setRoot(path);
                    render.writeTo(packages);
                    return;
                }
            }
        }

        throw new RuntimeException("NONE render for " + desc + " found.");
    }


}
