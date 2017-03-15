package org.coodex.concrete.common.struct;

import java.lang.reflect.Type;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class AbstractParam implements Annotated {

    /**
     * 参数类型
     *
     * @return
     */
    public abstract Class<?> getType();

    /**
     * 参数泛型类型
     *
     * @return
     */
    public abstract Type getGenericType();

    /**
     * 参数名
     *
     * @return
     */
    public abstract String getName();

    /**
     * 参数索引号
     *
     * @return
     */
    public abstract int getIndex();


}
