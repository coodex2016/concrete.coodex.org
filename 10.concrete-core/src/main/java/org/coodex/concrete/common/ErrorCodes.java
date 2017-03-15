package org.coodex.concrete.common;


/**
 * concrete 系统用到的错误号定义
 * Created by davidoff shen on 2016-09-01.
 */
public final class ErrorCodes extends AbstractErrorCodes {

    public static final int UNKNOWN_ERROR = CUSTOM_LOWER_BOUND - 1;

    public static final int MODULE_DEFINITION_NOT_FOUND = CONCRETE_CORE + 1;

    public static final int UNIT_DEFINITION_NOT_FOUND = CONCRETE_CORE + 2;

    public static final int NONE_TOKEN = CONCRETE_CORE + 3;

    public static final int TOKEN_INVALIDATE = CONCRETE_CORE + 4;

    public static final int NONE_ACCOUNT = CONCRETE_CORE + 5;

    public static final int NO_AUTHORIZATION = CONCRETE_CORE + 6;

    public static final int ACCOUNT_INVALIDATE = CONCRETE_CORE + 7;

    public static final int UNTRUSTED_ACCOUNT = CONCRETE_CORE + 8;

    public static final int DATA_VIOLATION = CONCRETE_CORE + 9;

    public static final int NO_BEAN_PROVIDER_FOUND = CONCRETE_CORE + 10;

    public static final int NO_SERVICE_INSTANCE_FOUND = CONCRETE_CORE + 11;

    public static final int BEAN_CONFLICT = CONCRETE_CORE + 12;

    public static final int OUT_OF_SERVICE_TIME = CONCRETE_CORE + 13;

}
