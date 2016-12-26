package cc.coodex.concurrent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by davidoff shen on 2016-09-05.
 */
public final class ExecutorWrapper {

    private static final Set<ExecutorService> executors = new HashSet<ExecutorService>();

    public static final <T extends ExecutorService> T wrap(T executorService) {
        // TODO 动态代理，当Executor shutdown或shutdownNow的时候脱离管理
        executors.add(executorService);
        return executorService;
    }

    public static void shutdown() {
        for (ExecutorService service : executors) {
            if (service != null && !service.isShutdown() && !service.isTerminated())
                service.shutdown();
        }
    }

    public static List<Runnable> shutdownNow() {
        List<Runnable> list = new ArrayList<Runnable>();
        for (ExecutorService service : executors) {
            if (service != null && !service.isTerminated())
                list.addAll(service.shutdownNow());
        }
        return list;
    }
}
