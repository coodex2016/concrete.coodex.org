package test.org.coodex.intf.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.AbstractInterceptor;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class ExampleInterceptor1 extends AbstractInterceptor {
    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
        System.out.println(111110);
        try {
            return super.around(context, joinPoint);
        }finally {
            System.out.println(111111);
        }
    }
}
