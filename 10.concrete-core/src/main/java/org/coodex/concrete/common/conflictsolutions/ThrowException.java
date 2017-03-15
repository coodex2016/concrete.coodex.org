package org.coodex.concrete.common.conflictsolutions;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConflictSolution;
import org.coodex.concrete.common.ErrorCodes;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public class ThrowException implements ConflictSolution {
    @Override
    public boolean accepted(Class<?> clazz) {
        return false;
    }

    @Override
    public <T> T conflict(Map<String, T> beans, Class<T> clz) {
        throw new ConcreteException(ErrorCodes.BEAN_CONFLICT, clz, beans.size());
    }
}
