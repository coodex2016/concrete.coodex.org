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

import org.coodex.util.Renderer;

import java.util.Date;

public class RendererTest {

    public static void main(String[] args) {
        System.out.println(Renderer.render(
                "您好，{0}。今天是{1,date,yyyy-MM-dd}，当前时间{1,time,HH:mm:ss}，您的服务号是{2,number,000}。祝您生活愉快。",
                "Davidoff", new Date(), 3));
    }

}
