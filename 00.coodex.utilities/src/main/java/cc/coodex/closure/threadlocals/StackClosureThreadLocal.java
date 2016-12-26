package cc.coodex.closure.threadlocals;

import cc.coodex.closure.Closure;

import java.util.Stack;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public class StackClosureThreadLocal<VariantType> extends ClosureThreadLocal<Stack<VariantType>> {

    public VariantType get() {
        Stack<VariantType> stack = $getVariant();
        return stack == null ? null : stack.lastElement();
    }


    public Object runWith(VariantType variant, Closure runnable) {
        if (runnable == null) return null;

        Stack<VariantType> stack = $getVariant();
        if (stack == null) {
            stack = new Stack<VariantType>();
            stack.push(variant);
            try {
                return closureRun(stack, runnable);
            } finally {
                stack.clear();
            }
        } else {
            stack.push(variant);
            try {
                return runnable.run();
            } finally {
                stack.pop();
            }
        }
    }

}
