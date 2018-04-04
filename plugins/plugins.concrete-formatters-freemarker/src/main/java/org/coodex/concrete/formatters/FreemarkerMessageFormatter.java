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

package org.coodex.concrete.formatters;

import org.coodex.concrete.common.LogFormatter;
import org.coodex.concrete.common.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用free marker模版引擎进行消息格式化
 * <p>
 * <p>
 * Created by davidoff shen on 2016-12-02.
 */
public class FreemarkerMessageFormatter extends AbstractFreemarkerFormatter implements MessageFormatter, LogFormatter {
    /**
     * @param pattern free marker引擎模版，o+index就是objects里的索引, 从1开始
     * @param objects
     * @return
     */
    @Override
    public String format(String pattern, Object... objects) {
        if (objects == null || objects.length == 0) return pattern;

        Map<String, Object> values = new HashMap<String, Object>();
        for (int i = 1; i <= objects.length; i++) {
            values.put("o" + i, objects[i - 1]);
        }
        return format(pattern, values);
//        try {
//            return super.formatMsg(pattern, values);
//        } catch (Throwable th) {
//            throw new RuntimeException(th.getLocalizedMessage(), th);
//        }
    }

    @Override
    public String format(String pattern, Map<String, Object> values) {
        try {
            return super.formatMsg(pattern, values);
        } catch (Throwable th) {
            throw new RuntimeException(th.getLocalizedMessage(), th);
        }
    }
}
