package org.coodex.concrete.attachments.client;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.attachments.Attachment;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.AbstractInterceptor;
import org.coodex.concrete.core.intercept.InterceptOrders;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.util.ReflectHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AttachmentInterceptor extends AbstractInterceptor {

    public static final int ORDER = InterceptOrders.OTHER + 1;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        try {
            Set<String> attachments = new HashSet<String>();
            grant(result, attachments, null, false);
            if (attachments.size() > 0) {
                ClientServiceImpl.allow(attachments);
            }
        } finally {
            return super.after(context, joinPoint, result);
        }
    }


    private void grant(Object object, Set<String> attachments, Set<Object> stack, boolean declaredAttachment) {
        if (object == null) return;

        if (stack == null) {
            stack = new HashSet<Object>();
        }

        if (stack.contains(object)) return;

        Class<?> c = object.getClass();
        if (c != String.class) stack.add(object);

        if (c == Object.class) {
            return;
        } else if (c.isArray()) {
            for (Object o : (Object[]) object) {
                grant(o, attachments, stack, declaredAttachment);
            }
        } else if (Iterable.class.isAssignableFrom(c)) {
            for (Object o : (Iterable) object) {
                grant(o, attachments, stack, declaredAttachment);
            }
        } else if (Map.class.isAssignableFrom(c)) {
            for (Object o : ((Map) object).values()) {
                grant(o, attachments, stack, declaredAttachment);
            }
        } else {
            if (!declaredAttachment) {
                for (Field f : ReflectHelper.getAllDeclaredFields(c)) {
                    f.setAccessible(true);
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    try {
                        grant(f.get(object), attachments, stack, f.getAnnotation(Attachment.class) != null);
                    } catch (IllegalAccessException e) {
                    }
                }
            } else {
                if (c == String.class) {
                    attachments.add((String) object);
                } else if (JaxRSHelper.isPrimitive(c)) {
                    return;
                } else {
                    grant(object, attachments, stack, false);
                }

            }

        }
    }
}
