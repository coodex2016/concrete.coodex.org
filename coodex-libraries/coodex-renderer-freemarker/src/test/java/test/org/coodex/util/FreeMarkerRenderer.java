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

package test.org.coodex.util;

import org.coodex.util.Common;
import org.coodex.util.Renderer;

import java.util.HashMap;
import java.util.Map;

public class FreeMarkerRenderer {
    public static void main(String[] args) {
        System.out.println(Renderer.render("现在时刻是 ${o1}", Common.now()));
        Map<String,Object> map = new HashMap<>();
        map.put("test","test");
        System.out.println(Renderer.render("测试：${o1.test}", map));
        System.out.println(Renderer.render("测试：${o2!\"xxx\"}", map));
    }
}
