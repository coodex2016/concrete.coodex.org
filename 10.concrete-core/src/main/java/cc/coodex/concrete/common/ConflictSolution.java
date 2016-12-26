package cc.coodex.concrete.common;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public interface ConflictSolution {

    boolean accepted(Class<?> clazz);

    <T> T conflict(Map<String, T> beans, Class<T> clz);
}
