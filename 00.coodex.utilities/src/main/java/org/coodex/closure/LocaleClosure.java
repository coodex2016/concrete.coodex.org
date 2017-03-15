package org.coodex.closure;

import org.coodex.closure.threadlocals.StackClosureThreadLocal;

import java.util.Locale;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public final class LocaleClosure {

    private final static StackClosureThreadLocal<Locale> localeClosure = new StackClosureThreadLocal<Locale>();

    public static Locale get() {
        return localeClosure.get();
    }

    public static void closureRun(Locale locale, Closure runnable) {
        localeClosure.runWith(locale, runnable);
    }

}
