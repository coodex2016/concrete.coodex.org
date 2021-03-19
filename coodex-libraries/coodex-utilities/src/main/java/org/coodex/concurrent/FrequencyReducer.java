package org.coodex.concurrent;

import org.coodex.config.Config;
import org.coodex.util.Singleton;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 减频器，用来降低执行体的执行频率
 *
 * @see Debounce
 * @see Throttle
 */
public interface FrequencyReducer {

    Singleton<ScheduledExecutorService> DEFAULT_REDUCER_EXECUTOR_SERVICE_SINGLETON = Singleton.with(
            () -> ExecutorsHelper.newScheduledThreadPool(
                    Config.getValue("frequency.reducer.executors.size", 3),
                    "FrequencyReducerPool"
            )
    );

    @Deprecated
    default void run() {
        submit();
    }

    void submit();


    @Deprecated
    default void run(Runnable runnable) {
        submit(runnable);
    }

    void submit(Runnable runnable);
}
