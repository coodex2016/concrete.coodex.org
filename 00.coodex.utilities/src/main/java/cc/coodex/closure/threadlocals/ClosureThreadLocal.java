package cc.coodex.closure.threadlocals;

import cc.coodex.closure.Closure;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public abstract class ClosureThreadLocal<VariantType> {


    private ThreadLocal<VariantType> threadLocal = new ThreadLocal<VariantType>();

    protected final VariantType $getVariant() {
        return threadLocal.get();
    }

    protected final Object closureRun(VariantType variant, Closure runnable) {
        if (runnable == null) return null;
        threadLocal.set(variant);
        try {
            return runnable.run();
        } finally {
            threadLocal.remove();
        }
    }


}
