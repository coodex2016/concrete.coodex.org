package cc.coodex.concrete.apitools.jaxrs.service;

import cc.coodex.concrete.apitools.jaxrs.AbstractRender;
import cc.coodex.concrete.apitools.jaxrs.DocToolkit;
import cc.coodex.concrete.apitools.jaxrs.POJOPropertyInfo;
import cc.coodex.util.ReflectHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * Created by davidoff shen on 2016-12-04.
 */
public class ServiceDocToolkit extends DocToolkit {
    public ServiceDocToolkit(AbstractRender render) {
        super(render);
    }

    private Set<String> pojoTypes = new HashSet<String>();

    public Set<String> getPojos() {
        return pojoTypes;
    }

    @Override
    protected String getTypeName(Class<?> clz) {
        try {
            return isPojo(clz) ? build(clz) : clz.getSimpleName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String build(Class<?> clz) throws IOException {
        if (!pojoTypes.contains(clz)) {
            List<POJOPropertyInfo> pojoPropertyInfos = new ArrayList<POJOPropertyInfo>();


            for (Method method : clz.getMethods()) {
                if (isProperty(method))
                    pojoPropertyInfos.add(new POJOPropertyInfo(clz, method));
            }

            for (Field field : ReflectHelper.getAllDeclaredFields(clz)) {
                if (isProperty(field))
                    pojoPropertyInfos.add(new POJOPropertyInfo(clz, field));
            }


            pojoTypes.add(canonicalName(clz.getName()));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("properties", pojoPropertyInfos);
            map.put("type", clz.getName());
            map.put("tool", this);

            getRender().writeTo("pojos/" + canonicalName(clz.getName()) + ".md", "pojo.md", map);
        }
        StringBuilder builder = new StringBuilder("[");
        builder.append(clz.getSimpleName()).append("](../pojos/").append(canonicalName(clz.getName())).append(".md)");
        return builder.toString();
    }

    private boolean isProperty(Field field) {
        int mod = field.getModifiers();
        return Modifier.isPublic(mod)
                && !Modifier.isStatic(mod)
                && !Modifier.isTransient(mod);
    }

    private boolean isProperty(Method method) {
        String name = method.getName();
        return method.getDeclaringClass() != Object.class
                && Modifier.isPublic(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && !Modifier.isTransient(method.getModifiers())
                && (name.startsWith("get") || (name.startsWith("is") && method.getReturnType() == boolean.class));

    }


}
