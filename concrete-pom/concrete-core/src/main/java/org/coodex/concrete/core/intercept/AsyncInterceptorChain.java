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
import org.coodex.concrete.common.DefinitionContext;

import java.util.*;

public class AsyncInterceptorChain extends AbstractInterceptor implements Set<ConcreteInterceptor>, InterceptorChain {


    private static Comparator<ConcreteInterceptor> comparatorAsc = new MyComparator(1);
    private static Comparator<ConcreteInterceptor> comparatorDesc = new MyComparator(-1);
    private Set<ConcreteInterceptor> interceptors = new HashSet<>();

    public AsyncInterceptorChain() {
    }

    public AsyncInterceptorChain(List<ConcreteInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
        return true;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        ConcreteInterceptor[] interceptors = this.interceptors.toArray(new ConcreteInterceptor[0]);
        Arrays.sort(interceptors, comparatorAsc);
        for (ConcreteInterceptor interceptor : interceptors) {
            if (interceptor.accept(context)) {
                interceptor.before(context, joinPoint);
            }
        }
    }

    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        ConcreteInterceptor[] interceptors = this.interceptors.toArray(new ConcreteInterceptor[0]);
        Arrays.sort(interceptors, comparatorDesc);

        for (ConcreteInterceptor interceptor : interceptors) {
            if (interceptor.accept(context)) {
                result = interceptor.after(context, joinPoint, result);
            }
        }
        return result;

    }

    @Override
    public Throwable onError(DefinitionContext context, MethodInvocation joinPoint, Throwable th) {
        ConcreteInterceptor[] interceptors = this.interceptors.toArray(new ConcreteInterceptor[0]);
        Arrays.sort(interceptors, comparatorDesc);

        for (ConcreteInterceptor interceptor : interceptors) {
            if (interceptor.accept(context)) {
                th = interceptor.onError(context, joinPoint, th);
                if (th == null) {
                    return null;
                }
            }
        }
        return th;
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

    @Override
    public Collection<ConcreteInterceptor> allInterceptors() {
        return interceptors;
    }

    private static class MyComparator implements Comparator<ConcreteInterceptor> {

        private final int sign;

        public MyComparator(int sign) {
            this.sign = sign;
        }

        public int $compare(ConcreteInterceptor o1, ConcreteInterceptor o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.getOrder() - o2.getOrder();
        }

        @Override
        public int compare(ConcreteInterceptor o1, ConcreteInterceptor o2) {
            return $compare(o1, o2) * sign;
        }
    }


}


