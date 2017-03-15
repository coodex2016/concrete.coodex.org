/**
 * 
 */
package org.coodex.pojomocker;

import java.util.NoSuchElementException;
import java.util.Stack;

/**
 *
 * 
 * @author davidoff
 *
 */
public class MockContextHelper {
   private final static ThreadLocal<Stack<MockContext>> stackThreadLocal = new ThreadLocal<Stack<MockContext>>();

   private final static ThreadLocal<Stack<StackTraceElement>> invokerThreadLocal = new ThreadLocal<Stack<StackTraceElement>>();

   private static Stack<MockContext> getStack() {
      Stack<MockContext> stack = stackThreadLocal.get();
      if (stack == null) {
         stack = new Stack<MockContext>();
         stackThreadLocal.set(stack);
      }
      return stack;
   }

   private static StackTraceElement getInvoker() {
      Throwable th = new Throwable();
      // invokerMethod {3}
      // --> enter() {2}
      // --> pushInvoker() {1}
      // --> getInvoker() {0}
      return th.getStackTrace()[3];
   }

   private static Stack<StackTraceElement> getInvokerStack() {
      Stack<StackTraceElement> stack = invokerThreadLocal.get();
      if (stack == null) {
         stack = new Stack<StackTraceElement>();
         invokerThreadLocal.set(stack);
      }
      return stack;
   }

   private static void accessCheck(StackTraceElement ste)
         throws IllegalAccessException {
      // TODO 更近一步控制安全，仅允许已明确的类型调用enter和leave
      // throw new IllegalAccessException();
   }

   private final static void pushInvoker() throws IllegalAccessException {
      StackTraceElement ste = getInvoker();
      getInvokerStack().push(ste);
      accessCheck(ste);
   }

   private final static void popInvoker() throws IllegalAccessException {
      StackTraceElement ste = getInvoker();
      accessCheck(ste);
      try {
         StackTraceElement steClosest = getInvokerStack().pop();
         if (!ste.getClassName().equals(steClosest.getClassName())
               || !ste.getMethodName().equals(steClosest.getMethodName())) {
            
            throw new IllegalAccessException("\nenter() invoked by: "
                  + steClosest.toString() + "\nleave() invoked by: "
                  + ste.toString());
         }
      } catch (NoSuchElementException e) {
         throw new IllegalAccessException("enter() NOT called.");
      }

   }

   public static MockContext currentContext() {
      Stack<MockContext> stack = getStack();
      if (stack.size() == 0)
         return null;
      else
         return stack.lastElement();
   }

   public final static MockContext enter() throws IllegalAccessException {
      pushInvoker();
      MockContext context = new MockContext(currentContext());
      getStack().push(context);
      return context;
   }

   public final static void leave() throws IllegalAccessException {
      popInvoker();
      Stack<MockContext> stack = getStack();
      if (stack.size() != 0) {
         stack.pop();
      }
   }

}
