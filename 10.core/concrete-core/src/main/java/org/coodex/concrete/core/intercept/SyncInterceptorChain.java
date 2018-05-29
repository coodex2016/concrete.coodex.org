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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class SyncInterceptorChain extends AbstractSyncInterceptor implements Set<ConcreteInterceptor> {

    private static Comparator<ConcreteInterceptor> comparator = new Comparator<ConcreteInterceptor>() {
        @Override
        public int compare(ConcreteInterceptor o1, ConcreteInterceptor o2) {
            if (o1 == o2) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            return o1.getOrder() - o2.getOrder();
        }
    };
    private Set<ConcreteInterceptor> interceptors = new HashSet<ConcreteInterceptor>();

    public SyncInterceptorChain() {
    }

    public SyncInterceptorChain(List<ConcreteInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return true;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return super.invoke(new MethodInvocationChain(createQueue(), invocation));
    }

    private Queue<ConcreteSyncInterceptor> createQueue() {
        Queue<ConcreteSyncInterceptor> queue = new LinkedList<ConcreteSyncInterceptor>();
        ConcreteInterceptor[] interceptors = this.interceptors.toArray(new ConcreteInterceptor[0]);
        Arrays.sort(interceptors, comparator);

        for (ConcreteInterceptor interceptor : interceptors) {
            if (interceptor != null && !(interceptor instanceof SyncInterceptorChain)) {
                queue.add(
                        interceptor instanceof ConcreteSyncInterceptor ?
                                (ConcreteSyncInterceptor) interceptor :
                                asyncToSync(interceptor));
            }
        }
        return queue;
    }

    @Override
    public int size() {
        return interceptors.size();
    }


    ///////////////////// 实现set接口

    @Override
    public boolean isEmpty() {
        return interceptors.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return interceptors.contains(o);
    }

    @Override
    public Iterator<ConcreteInterceptor> iterator() {
        return interceptors.iterator();
    }

    @Override
    public Object[] toArray() {
        return interceptors.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return interceptors.toArray(a);
    }

    @Override
    public boolean add(ConcreteInterceptor concreteInterceptor) {
        return interceptors.add(concreteInterceptor);
    }

    @Override
    public boolean remove(Object o) {
        return interceptors.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return interceptors.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends ConcreteInterceptor> c) {
        return interceptors.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return interceptors.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return interceptors.removeAll(c);
    }

    @Override
    public void clear() {
        interceptors.clear();
    }

    private static class MethodInvocationChain implements MethodInvocation {

        private final Queue<ConcreteSyncInterceptor> queue;
        private final MethodInvocation invocation;

        public MethodInvocationChain(Queue<ConcreteSyncInterceptor> queue, MethodInvocation invocation) {
            this.queue = queue;
            this.invocation = invocation;
        }

        @Override
        public Method getMethod() {
            return invocation.getMethod();
        }

        @Override
        public Object[] getArguments() {
            return invocation.getArguments();
        }

        @Override
        public Object proceed() throws Throwable {
            if (queue.isEmpty())
                return invocation.proceed();
            ConcreteSyncInterceptor interceptor = queue.poll();
            return interceptor.invoke(new MethodInvocationChain(queue, invocation));
        }

        @Override
        public Object getThis() {
            return invocation.getThis();
        }

        @Override
        public AccessibleObject getStaticPart() {
            return invocation.getStaticPart();
        }
    }


}
