package org.coodex.concrete.core.intercept;


/**
 * Created by davidoff shen on 2016-09-01.
 */
public class InterceptOrders {

    /**
     * 审计切片
     */
    public static final int SYSTEM_AUDIT = 100;

    /**
     * 系统服务时间
     */
    public static final int SERVICE_TIMING = 200;

    /**
     * Bean有效性验证切片
     */
    public static final int BEAN_VALIDATION = 1000;

    /**
     * RBAC切片
     */
    public static final int RBAC = 9000;


    public static final int OTHER = 9001;
}
