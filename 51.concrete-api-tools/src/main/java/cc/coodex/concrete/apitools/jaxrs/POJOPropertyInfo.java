package cc.coodex.concrete.apitools.jaxrs;

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
        pojoTypeInfo = new POJOTypeInfo(contextType,method.getGenericReturnType());
//        type = method.getGenericReturnType();
    }

    public POJOPropertyInfo(Class<?> contextType, Field field) {
        name = field.getName();
        pojoTypeInfo = new POJOTypeInfo(contextType, field.getGenericType());
//        type = field.getGenericType();
    }

    private final String name;
    private POJOTypeInfo pojoTypeInfo;

//    private final Type type;

    public String getName() {
        return name;
    }

    public POJOTypeInfo getType() {
        return pojoTypeInfo;
    }

//    public String getTypeString(){
//
//    }
}
