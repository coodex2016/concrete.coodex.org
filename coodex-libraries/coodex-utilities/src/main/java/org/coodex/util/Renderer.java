/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 渲染器统一接口
 */

public class Renderer {

    private final static Logger log = LoggerFactory.getLogger(Renderer.class);


    private static final SelectableServiceLoader<String, RenderService> RENDER_SERVICE_LOADER
            = new LazySelectableServiceLoader<String, RenderService>(new DefaultRenderer()) {
    };

    /**
     * @param template 待渲染的模板
     * @param objects  渲染参数，支持{@link java.util.function.Supplier}
     * @return 渲染后的字符串
     */
    public static String render(String template, Object... objects) {
        RenderService renderService = RENDER_SERVICE_LOADER.select(template);
        try {
            return RENDER_SERVICE_LOADER.select(template).render(template, objects);
        } catch (Throwable th) {
            log.warn("render failed: [RenderService: {}, template: {}]", renderService, template, th);
            throw Common.rte(th);
        }
    }

    /**
     * 默认的Renderer，返回带渲染的模板，不使用参数
     */
    static class DefaultRenderer implements RenderService {
        @Override
        public String render(String template, Object... objects) {
            return template;
        }

        @Override
        public boolean accept(String param) {
            return true;
        }
    }


}
