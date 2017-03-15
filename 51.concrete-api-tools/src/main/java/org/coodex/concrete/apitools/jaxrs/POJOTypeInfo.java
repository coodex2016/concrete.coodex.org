package org.coodex.concrete.apitools.jaxrs;

import org.coodex.util.TypeHelper;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public class POJOTypeInfo {

    private static final Class<?> ARRAY_CLASS = (new Object[0]).getClass();
    private static final POJOTypeInfo OBJECT_POJO_INFO = new POJOTypeInfo(Object.class, Object.class);

    private final Class<?> contextType;

    private final Type genericType;

    private final Class<?> type;

    private POJOTypeInfo arrayElement = OBJECT_POJO_INFO;

    public POJOTypeInfo(Class<?> contextType, Type genericType) {
        this.contextType = contextType;
        this.genericType = genericType;
        this.type = loadClass();
    }

    private List<POJOTypeInfo> genericParameters = new ArrayList<POJOTypeInfo>();

    private Class<?> loadClass() {
        if (genericType instanceof GenericArrayType) {
            arrayElement = new POJOTypeInfo(contextType, ((GenericArrayType) genericType).getGenericComponentType());
            return ARRAY_CLASS;
        } else if (genericType instanceof ParameterizedType) {
            Class<?> clz = (Class<?>) ((ParameterizedType) genericType).getRawType();
            for (Type t : ((ParameterizedType) genericType).getActualTypeArguments()) {
                genericParameters.add(new POJOTypeInfo(contextType, t));
            }
            return clz;
        } else if (genericType instanceof TypeVariable) {
            return (Class<?>) TypeHelper.findActualClassFrom((TypeVariable) genericType, contextType);
        } else if (genericType instanceof Class) {
            if(((Class) genericType).isArray()){
                arrayElement = new POJOTypeInfo(contextType, ((Class) genericType).getComponentType());
                return ARRAY_CLASS;
            }
            return (Class<?>) genericType;
        }
        throw new RuntimeException("unknown Type: " + genericType);
    }

    public Type getGenericType() {
        return genericType;
    }

    public Class<?> getType() {
        return type;
    }

    public POJOTypeInfo getArrayElement() {
        return arrayElement;
    }

    public List<POJOTypeInfo> getGenericParameters() {
        return genericParameters;
    }
}
