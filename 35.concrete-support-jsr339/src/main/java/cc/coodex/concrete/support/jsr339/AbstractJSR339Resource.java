package cc.coodex.concrete.support.jsr339;

import cc.coodex.concrete.api.ConcreteService;
import cc.coodex.concrete.common.BeanProviderFacade;
import cc.coodex.concrete.common.ConcreteClosure;
import cc.coodex.concrete.common.ConcreteHelper;
import cc.coodex.concrete.core.token.TokenWrapper;
import cc.coodex.concrete.jaxrs.AbstractJAXRSResource;
import cc.coodex.concurrent.ExecutorsHelper;
import cc.coodex.concurrent.components.PriorityRunnable;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;


/**
 * Created by davidoff shen on 2016-11-25.
 */
public abstract class AbstractJSR339Resource<T extends ConcreteService> extends AbstractJAXRSResource<T> {

    @Override
    protected int getMethodStartIndex() {
        return 2;
    }

    private final static Executor EXECUTOR = ExecutorsHelper.newPriorityThreadPool(
            ConcreteHelper.getProfile().getInt("jsr339.threadpool.corePoolSize", 0),
            ConcreteHelper.getProfile().getInt("jsr339.threadpool.maximumPoolSize", Integer.MAX_VALUE)
    );


    /**
     * @return
     */
    private static Executor getExecutor() {
        return EXECUTOR;
    }


    protected void __execute(final String methodName, final AsyncResponse asyncResponse, final String tokenId, final Object... params) {

        final Method method = findMethod(methodName, null);

        getExecutor().execute(new PriorityRunnable(getPriority(method), new Runnable() {
            @Override
            public void run() {
                try {

                    asyncResponse.resume(invokeByTokenId(tokenId, method, params));
                } catch (Throwable th) {
                    asyncResponse.resume(th);
                }
            }
        }));

    }

    protected void execute(String invokeMethodName, AsyncResponse asyncResponse, String tokenId, Object... objects) {
        __execute(invokeMethodName, asyncResponse, tokenId, objects);
    }


}
