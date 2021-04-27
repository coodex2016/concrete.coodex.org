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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.config.Config;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-01.
 */
public class API {
    private static final ServiceLoader<ConcreteAPIRenderer> RENDERS =
            new LazyServiceLoader<ConcreteAPIRenderer>() {
            };
    private static final String TAG_API_GENERATOR = "api_gen";

    public static void generate(String desc, String path, String... packages) throws IOException {
//        if (packages == null) {
//            packages = ConcreteHelper.getApiPackages();
//        }
//        if (RENDERS.getAllInstances().size() == 0)
//            throw new RuntimeException("NONE render found.");
//        for (ConcreteAPIRender render : RENDERS.getAllInstances()) {
//            synchronized (render) {
//                if (render.isAccept(desc)) {
//                    render.setRoot(path);
//                    render.writeTo(packages);
//                    return;
//                }
//            }
//        }
//
//        throw new RuntimeException("NONE render for " + desc + " found.");

        generate(null, desc, path, packages);
    }


    public static void generate(Map<String, Object> ext, String desc, String path, String... packages) throws IOException {
        if (packages == null) {
            packages = ConcreteHelper.getApiPackages();
        }
        if (RENDERS.getAll().size() == 0)
            throw new RuntimeException("NONE render found.");
        for (ConcreteAPIRenderer render : RENDERS.getAll().values()) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (render) {
                if (render.isAccept(desc)) {
                    render.setRoot(path);
                    render.setExt(ext);
                    render.writeTo(packages);
                    return;
                }
            }
        }

        throw new RuntimeException("NONE render for " + desc + " found.");
    }

    /**
     * 根据api_gen.properties（or api_gen.module.properties）的配置生成所需的内容
     *
     * @param module module
     */
    @SuppressWarnings("unused")
    public static void generateFor(String module, String... packages) throws IOException {
        String desc = Config.get("desc", TAG_API_GENERATOR, module);
        String path = Config.get("path", TAG_API_GENERATOR, module);

        generate(toMap(Config.get("ext", TAG_API_GENERATOR, module)),
                desc, path, packages);
    }

    private static Map<String, Object> toMap(String json) {
        if (json == null)
            return new HashMap<>();
        else {
            return JSON.parseObject(json, new TypeReference<Map<String, Object>>() {
            });
        }
    }


}
