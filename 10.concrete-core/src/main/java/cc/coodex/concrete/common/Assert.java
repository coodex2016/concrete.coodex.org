package cc.coodex.concrete.common;

/**
 * Created by davidoff shen on 2016-08-29.
 */
public final class Assert {

    /**
     * 当表达式exp为真时，抛出code异常
     *
     * @param exp
     * @param code
     * @param objects
     */
    public static final void is(boolean exp, int code, Object... objects) {
        if (exp)
            throw new ConcreteException(code, objects);
    }

    /**
     * 表达式为否事，抛出code异常
     *
     * @param exp
     * @param code
     * @param objects
     */
    public static final void not(boolean exp, int code, Object... objects) {
        if (!exp)
            throw new ConcreteException(code, objects);
    }

    /**
     * 当对象o为null是，抛出code异常
     *
     * @param o
     * @param code
     * @param objects
     */
    public static final void isNull(Object o, int code, Object... objects) {
        is(o == null, code, objects);
    }

    /**
     * 当对象o不为null时，抛出code异常
     *
     * @param o
     * @param code
     * @param objects
     */
    public static final void notNull(Object o, int code, Object... objects) {
        is(o != null, code, objects);
    }


}
