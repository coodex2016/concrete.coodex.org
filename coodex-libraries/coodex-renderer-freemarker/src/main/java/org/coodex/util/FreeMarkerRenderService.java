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

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FreeMarkerRenderService implements RenderService {
    public static final Pattern PATTERN = Pattern.compile("\\$\\{o[0-9]+([.?!][^]]+)?}");

    private static final Configuration FREEMARKER = new Configuration(Configuration.getVersion());

    private static final StringTemplateLoader TEMPLATE_LOADER = new StringTemplateLoader();

    private static final SingletonMap<String, Template> TEMPLATE_SINGLETON_MAP
            = SingletonMap.<String, Template>builder()
            .function(FreeMarkerRenderService::getTemplate)
            .build();

    static {
        FREEMARKER.setTemplateLoader(TEMPLATE_LOADER);
    }

    @SneakyThrows
    private static Template getTemplate(String template) {
        synchronized (TEMPLATE_LOADER) {
            TEMPLATE_LOADER.putTemplate(template, template);
        }
        return FREEMARKER.getTemplate(template);
    }


    @SneakyThrows
    private static String formatMsg(String template, Map<String, Object> values) {
        if (template == null) return null;
        Template t = TEMPLATE_SINGLETON_MAP.get(template);
        try (Writer writer = new StringWriter()) {
            t.process(values, writer);
            return writer.toString();
        }
    }

    private static Map<String, Object> toMap(Object[] objects) {
        Map<String, Object> values = new HashMap<>();
        for (int i = 1; i <= objects.length; i++) {
            values.put("o" + i, objects[i - 1]);
        }
        return values;
    }

//    public static void main(String[] args) {
//        System.out.println(Renderer.render("现在时刻是 ${o1}", Common.now()));
//        Map<String, Object> map = new HashMap<>();
//        map.put("test", "test");
//        System.out.println(Renderer.render("测试：${o1.test}", map));
//        System.out.println(Renderer.render("测试：${o2!\"xxx\"}", map));
//    }

    @Override
    public String render(String template, Object... objects) {
        if (objects == null || objects.length == 0) return template;
        return formatMsg(template, toMap(transfer(objects)));
    }

    @Override
    public boolean accept(String param) {
        return param != null && PATTERN.matcher(param).find();
    }
}
