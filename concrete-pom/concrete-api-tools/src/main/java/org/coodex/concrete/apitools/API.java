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
import org.coodex.concrete.common.modules.AbstractModule;
import org.coodex.config.Config;
import org.coodex.exception.NoneInstanceException;
import org.coodex.exception.NoneSupportedException;
import org.coodex.util.Common;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by davidoff shen on 2016-12-01.
 */
public class API {

    private final Set<String> packages = new HashSet<>();
    private final Set<Class<?>> classSet = new HashSet<>();
    private final String desc;
    private final String path;

    //    private boolean clean;
    private final Map<String, Object> ext = new HashMap<>();

    private API(Builder builder) {
        this.desc = builder.desc;
        this.packages.addAll(builder.packages);
        this.classSet.addAll(builder.classSet);
        this.path = builder.path;
//        this.clean = builder.clean;
        this.ext.putAll(builder.ext);
    }

    public void generate() throws IOException {
        if (RENDERS.getAll().size() == 0)
            throw new NoneInstanceException("NONE render found.");
//        if (clean) {
//            cleanDir();
//        }
        for (ConcreteAPIRenderer<?> render : RENDERS.getAll().values()) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (render) {
                if (render.isAccept(desc)) {
                    render.setRoot(path);
                    render.setExt(ext);
                    render.render(Common.cast(getModules()));
//                    render.writeTo(packages);
                    return;
                }
            }
        }
        throw new NoneSupportedException("NONE render for " + desc + " found.");
    }

    private void deleteFiles(File file) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    deleteFiles(f);
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    private void cleanDir() {
        deleteFiles(new File(path));
    }

    private List<AbstractModule<?>> getModules() {
        List<AbstractModule<?>> result;
        if (Common.isEmpty(packages)) {
            result = APIHelper.loadModules(desc, ConcreteHelper.getApiPackages());
        } else {
            result = APIHelper.loadModules(desc, packages.toArray(new String[0]));
        }
        Set<Class<?>> loaded = result.stream().map(AbstractModule::getInterfaceClass).collect(Collectors.toSet());
        Set<Class<?>> noneLoaded = Common.difference(classSet, loaded);
        if (!Common.isEmpty(noneLoaded)) {
            result.addAll(APIHelper.loadModules(desc, noneLoaded.toArray(new Class[0])));
        }
        return result;
    }

    public static class Builder {
        private final Set<String> packages = new HashSet<>();
        private final Set<Class<?>> classSet = new HashSet<>();
        private String desc;
        private String path;

        //        private boolean clean = true;
        private final Map<String, Object> ext = new HashMap<>();

        public Builder addPackages(String... packages) {
            if (packages != null && packages.length > 0) {
                this.packages.addAll(Arrays.asList(packages));
            }
            return this;
        }

        public Builder addPackages(Package... packages) {
            if (packages != null && packages.length > 0) {
                this.packages.addAll(Arrays.stream(packages).map(Package::getName).collect(Collectors.toSet()));
            }
            return this;
        }

        public Builder addClasses(Class<?>... classes) {
            if (classes != null && classes.length > 0) {
                this.classSet.addAll(Arrays.asList(classes));
            }
            return this;
        }

        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder ext(Map<String, Object> map) {
            if (map != null && !map.isEmpty()) {
                this.ext.putAll(map);
            }
            return this;
        }

        public Builder module(String module) {
            return this.desc(Config.get("desc", TAG_API_GENERATOR, module))
                    .path(Config.get("path", TAG_API_GENERATOR, module))
                    .ext(toMap(Config.get("ext", TAG_API_GENERATOR, module)));
        }


        public API build() {
            return new API(this);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private static final ServiceLoader<ConcreteAPIRenderer<?>> RENDERS =
            new LazyServiceLoader<ConcreteAPIRenderer<?>>() {
            };
    private static final String TAG_API_GENERATOR = "api_gen";

    @Deprecated
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
//        newBuilder().desc(desc).path(path)
//                .addPackages(packages)
//                .build().generate();
    }

    @Deprecated
    public static void generate(Map<String, Object> ext, String desc, String path, String... packages) throws IOException {
//        if (packages == null) {
//            packages = ConcreteHelper.getApiPackages();
//        }
        generate(ext, desc, path, APIHelper.loadModules(desc, packages));
//        newBuilder().desc(desc).path(path)
//                .ext(ext).addPackages(packages)
//                .build().generate();

    }

    @Deprecated
    private static void generate(Map<String, Object> ext,
                                 String desc,
                                 String path,
                                 List<? extends AbstractModule<?>> modules) throws IOException {
        if (RENDERS.getAll().size() == 0)
            throw new RuntimeException("NONE render found.");
        for (ConcreteAPIRenderer<?> render : RENDERS.getAll().values()) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (render) {
                if (render.isAccept(desc)) {
                    render.setRoot(path);
                    render.setExt(ext);
                    render.render(Common.cast(modules));
//                    render.writeTo(packages);
                    return;
                }
            }
        }
        throw new RuntimeException("NONE render for " + desc + " found.");
    }

    //
    @Deprecated
    public static void generate(Map<String, Object> ext, String desc, String path, Class<?>... classes) throws IOException {
        if (classes == null) {
            generate(ext, desc, path, (String[]) null);
            return;
        }
        generate(ext, desc, path, APIHelper.loadModules(desc, classes));
    }

    /**
     * 根据api_gen.properties（or api_gen.module.properties）的配置生成所需的内容
     *
     * @param module module
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static void generateFor(String module, String... packages) throws IOException {
        String desc = Config.get("desc", TAG_API_GENERATOR, module);
        String path = Config.get("path", TAG_API_GENERATOR, module);

        generate(toMap(Config.get("ext", TAG_API_GENERATOR, module)),
                desc, path, packages);
    }


    @Deprecated
    public static void generateFor(String module, Class<?>... classes) throws IOException {
        String desc = Config.get("desc", TAG_API_GENERATOR, module);
        String path = Config.get("path", TAG_API_GENERATOR, module);

        generate(toMap(Config.get("ext", TAG_API_GENERATOR, module)),
                desc, path, classes);
    }

    private static Map<String, Object> toMap(String json) {
        if (json == null) {
            return new HashMap<>();
        } else {
            return JSON.parseObject(json, new TypeReference<Map<String, Object>>() {
            });
        }
    }


}
