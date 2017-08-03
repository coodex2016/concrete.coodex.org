package test.org.coodex.intf.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.AbstractInterceptor;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class ExampleInterceptor2 extends AbstractInterceptor {

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
        System.out.println(222220);
        try {
            return super.around(context, joinPoint);
        } finally {
            System.out.println(222222);
        }
    }
}
