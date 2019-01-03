/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.fsm.impl;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.fsm.IdentifiedState;
import org.coodex.concrete.fsm.IdentifiedStateContainer;
import org.coodex.concrete.fsm.IdentifiedStateIsLockingException;
import org.coodex.concrete.fsm.IdentifiedStateLoader;
import org.coodex.util.TypeHelper;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SingletonIdentifiedStateContainer implements IdentifiedStateContainer {

    private final static Map<Serializable, Lock> STATUS_MAP = new ConcurrentHashMap<Serializable, Lock>();

//    private static ScheduledExecutorService LOCK_FORCE_RELEASE = null;
    private final static ConcreteServiceLoader<IdentifiedStateLoader> LOADERS = new ConcreteServiceLoader<IdentifiedStateLoader>() {
    };
    private final static long DEFAULT_TIME_OUT = 1000;
//    private static Singleton<ScheduledExecutorService> LOCK_FORCE_RELEASE =
//            new Singleton<ScheduledExecutorService>(new Singleton.Builder<ScheduledExecutorService>() {
//                @Override
//                public ScheduledExecutorService build() {
//                    return ConcreteHelper.getScheduler("fsm.lock.release");
////                    return ExecutorsHelper.newSingleThreadScheduledExecutor();
//                }
//            });
    private static Map<Class, IdentifiedStateLoader> loaderMap = new HashMap<Class, IdentifiedStateLoader>();

    private static ScheduledExecutorService getReleaseExecutor() {
//        if (LOCK_FORCE_RELEASE == null)
//            synchronized (SingletonIdentifiedStateContainer.class) {
//                if (LOCK_FORCE_RELEASE == null) {
//                    LOCK_FORCE_RELEASE = ExecutorsHelper.newSingleThreadScheduledExecutor();
//                }
//            }
//        return LOCK_FORCE_RELEASE;
//        return LOCK_FORCE_RELEASE.getInstance();
        return ConcreteHelper.getScheduler("fsm.lock.release");
    }


    @SuppressWarnings("unchecked")
    private static <S extends IdentifiedState<ID>, ID extends Serializable, L extends IdentifiedStateLoader<S, ID>>
    L getLoader(Class<? extends S> stateClass) {
        synchronized (loaderMap) {
            if (!loaderMap.containsKey(stateClass)) {
                Type t = IdentifiedStateLoader.class.getTypeParameters()[0];
                boolean found = false;
                for (IdentifiedStateLoader loader : LOADERS.getAllInstances()) {
                    if (stateClass.equals(TypeHelper.toTypeReference(t, loader.getClass()))) {
                        loaderMap.put(stateClass, loader);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("No IdentifiedStateLoader found for " + stateClass.getClass().getName());
                }
            }
            return (L) loaderMap.get(stateClass);
        }

    }

    @Override
    public <S extends IdentifiedState<ID>, ID extends Serializable> S newStateAndLock(Class<? extends S> stateClass) {
        return newStateAndLock(stateClass, DEFAULT_TIME_OUT);
    }

    @Override
    public <S extends IdentifiedState<ID>, ID extends Serializable> S newStateAndLock(Class<? extends S> stateClass, long timeout) {
        synchronized (STATUS_MAP) {
            return lock(getLoader(stateClass).newState(), timeout);
        }
    }

    private <S extends IdentifiedState<ID>, ID extends Serializable> S lock(final S state, long timeout) {
        if (timeout <= 0) timeout = DEFAULT_TIME_OUT;
        STATUS_MAP.put(state.getId(), new Lock(state, getReleaseExecutor().schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        $release(state.getId(), false);
                    }
                }, timeout, TimeUnit.MILLISECONDS
        )));
        return state;
    }

    @Override
    public <S extends IdentifiedState<ID>, ID extends Serializable> S loadStateAndLock(ID id, Class<? extends S> stateClass) throws IdentifiedStateIsLockingException {
        return loadStateAndLock(id, stateClass, DEFAULT_TIME_OUT);
    }

    @Override
    public <S extends IdentifiedState<ID>, ID extends Serializable> S loadStateAndLock(ID id, Class<? extends S> stateClass, long timeoutInMS) throws IdentifiedStateIsLockingException {
        synchronized (STATUS_MAP) {
            if (STATUS_MAP.containsKey(id)) {
                throw new IdentifiedStateIsLockingException(id);
            }
            return lock(getLoader(stateClass).getState(id), timeoutInMS);
        }
    }

    private void $release(Serializable id, boolean cancelTask) {
        synchronized (STATUS_MAP) {
            if (STATUS_MAP.containsKey(id)) {
                Lock lock = STATUS_MAP.remove(id);
                if (cancelTask)
                    lock.getFuture().cancel(true);
            }
        }
    }

    @Override
    public void release(Serializable id) {
        $release(id, true);
    }

    private static class Lock {
        private IdentifiedState identifiedState;
        private Future future;

        public Lock(IdentifiedState identifiedState, Future future) {
            this.identifiedState = identifiedState;
            this.future = future;
        }

        public IdentifiedState getIdentifiedState() {
            return identifiedState;
        }

        public Future getFuture() {
            return future;
        }
    }
}
