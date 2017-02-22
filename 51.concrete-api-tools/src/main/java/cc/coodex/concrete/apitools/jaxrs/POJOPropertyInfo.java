package cc.coodex.concrete.apitools.jaxrs;

import cc.coodex.concrete.api.Description;
import cc.coodex.util.Common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static cc.coodex.concrete.jaxrs.JaxRSHelper.lowerFirstChar;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public class POJOPropertyInfo {

    public POJOPropertyInfo(Class<?> contextType, Method method) {
        String methodName = method.getName();
        int startIndex = methodName.startsWith("is") && method.getReturnType() == boolean.class ? 2 : 3;
        name = lowerFirstChar(method.getName().substring(startIndex));
        pojoTypeInfo = new POJOTypeInfo(contextType, method.getGenericReturnType());
        description = method.getAnnotation(Description.class);
//        type = method.getGenericReturnType();
    }

    public POJOPropertyInfo(Class<?> contextType, Field field) {
        name = field.getName();
        description = field.getAnnotation(Description.class);
        pojoTypeInfo = new POJOTypeInfo(contextType, field.getGenericType());
//        type = field.getGenericType();
    }

    private final String name;
    private final Description description;
    private POJOTypeInfo pojoTypeInfo;

//    private final Type type;

    public String getName() {
        return name;
    }

    public POJOTypeInfo getType() {
        return pojoTypeInfo;
    }

    public String getLabel() {
        return description == null ? "" : description.name();
//        return Common.isBlank(s) ? "　" : s;
    }

    public String getDescription() {
        return description == null ? "" : description.description();
//        return Common.isBlank(s) ? "　" : s;
    }

//    public String getTypeString(){
//
//    }
}
