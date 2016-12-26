package cc.coodex.concrete.common;

import cc.coodex.util.Common;

/**
 * ConcreteException， Concrete异常.
 * <p>
 * Created by davidoff shen on 2016-08-29.
 */
public class ConcreteException extends RuntimeException {

    private int code;
    private Object[] o;

    /**
     * 根据系统的异常代码构建
     *
     * @param code
     */
    public ConcreteException(int code, Object... objects) {
        this.code = code;
        this.o = objects;
    }


    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        String message = ErrorMessageFacade.getMessage(code, o);
        return Common.isBlank(message) ? String.format("error code: %06d", code) : message;
    }

}
