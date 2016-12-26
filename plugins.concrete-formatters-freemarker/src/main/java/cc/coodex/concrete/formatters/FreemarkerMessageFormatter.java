package cc.coodex.concrete.formatters;

import cc.coodex.concrete.common.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用free marker模版引擎进行消息格式化
 * <p>
 * <p>
 * Created by davidoff shen on 2016-12-02.
 */
public class FreemarkerMessageFormatter extends AbstractFreemarkerFormatter implements MessageFormatter {
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
        try {
            return super.format(pattern, values);
        } catch (Throwable th) {
            throw new RuntimeException(th.getLocalizedMessage(), th);
        }
    }
}
