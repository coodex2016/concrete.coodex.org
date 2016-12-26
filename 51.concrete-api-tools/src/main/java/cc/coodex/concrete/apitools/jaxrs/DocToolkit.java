package cc.coodex.concrete.apitools.jaxrs;

import cc.coodex.concrete.jaxrs.JaxRSHelper;
import cc.coodex.util.Common;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import static cc.coodex.concrete.jaxrs.JaxRSHelper.isPrimitive;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public abstract class DocToolkit {

    private AbstractRender render;

    public static boolean isPojo(Class<?> type) {
        return !(isPrimitive(type) ||
                type.isArray() ||
                Collection.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type));
    }

    public DocToolkit(AbstractRender render) {
        this.render = render;
    }

    public String canonicalName(String name) {
        return canonicalName(name, "\\/");
    }

    public String canonicalName(String name, String delim) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer stringTokenizer = new StringTokenizer(name, delim);
        while (stringTokenizer.hasMoreTokens()) {
            String s = stringTokenizer.nextToken();
            if (Common.isBlank(s)) continue;
            if (builder.length() > 0) builder.append("_");
            builder.append(s);
        }
        return builder.toString();
    }

    public String formatTypeStr(Type t, Class<?> contextClass) {
        return formatPOJOTypeInfo(new POJOTypeInfo(contextClass, t));
    }

    public String formatPOJOTypeInfo(POJOTypeInfo info) {
        if(info.getType() == null){
            return info.getGenericType().toString();
        }

        if (info.getType().isArray()) {
            return formatPOJOTypeInfo(info.getArrayElement()) + "[]";
        } else {
            StringBuilder builder = new StringBuilder(getTypeName(info.getType()));
            if (info.getGenericParameters().size() > 0) {
                builder.append("<");
                boolean isFirst = true;
                for (POJOTypeInfo param : info.getGenericParameters()) {
                    if (!isFirst) builder.append(", ");
                    builder.append(formatPOJOTypeInfo(param));
                    if (isFirst) {
                        isFirst = false;
                    }
                }
                builder.append(">");
            }
            return builder.toString();
        }
    }

    protected abstract String getTypeName(Class<?> clz);

    public AbstractRender getRender() {
        return render;
    }

    public String camelCase(String str){
        return JaxRSHelper.camelCase(str);
    }
}
