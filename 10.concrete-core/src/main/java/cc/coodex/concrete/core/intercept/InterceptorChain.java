package cc.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class InterceptorChain extends AbstractInterceptor implements Set<AbstractInterceptor> {

    private static class MethodInvocationChain implements MethodInvocation {

        private Queue<AbstractInterceptor> queue;
        private MethodInvocation invocation;

        public MethodInvocationChain(Queue<AbstractInterceptor> queue, MethodInvocation invocation) {
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
            AbstractInterceptor interceptor = queue.poll();
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

    private static Comparator<AbstractInterceptor> comparator = new Comparator<AbstractInterceptor>() {
        @Override
        public int compare(AbstractInterceptor o1, AbstractInterceptor o2) {
            if (o1 == o2) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            return o1.getOrder() - o2.getOrder();
        }
    };

    @Override
    public int getOrder() {
        return -1;
    }

    private Set<AbstractInterceptor> interceptors = new HashSet<AbstractInterceptor>();

    public InterceptorChain() {
    }

    public InterceptorChain(List<AbstractInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return super.invoke(new MethodInvocationChain(createQueue(), invocation));
    }

    private Queue<AbstractInterceptor> createQueue() {
        Queue<AbstractInterceptor> queue = new LinkedList<AbstractInterceptor>();
        AbstractInterceptor[] interceptors = this.interceptors.toArray(new AbstractInterceptor[0]);
        Arrays.sort(interceptors, comparator);

        for (AbstractInterceptor interceptor : interceptors) {
            if (interceptor != null && !(interceptor instanceof InterceptorChain))
                queue.add(interceptor);
        }
        return queue;
    }


    ///////////////////// 实现set接口

    @Override
    public int size() {
        return interceptors.size();
    }

    @Override
    public boolean isEmpty() {
        return interceptors.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return interceptors.contains(o);
    }

    @Override
    public Iterator<AbstractInterceptor> iterator() {
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
    public boolean add(AbstractInterceptor abstractInterceptor) {
        return interceptors.add(abstractInterceptor);
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
    public boolean addAll(Collection<? extends AbstractInterceptor> c) {
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


}
