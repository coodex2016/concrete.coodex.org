package org.coodex.concrete.apitools.jaxrs.jquery;

import com.alibaba.fastjson.JSON;
import org.coodex.concrete.apitools.jaxrs.AbstractRender;
import org.coodex.concrete.apitools.jaxrs.service.ServiceDocToolkit;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.pojomocker.POJOMocker;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class JQueryDocToolkit extends ServiceDocToolkit {

    public JQueryDocToolkit(AbstractRender render) {
        super(render);
    }

    public String camelCase(String s){
        return JaxRSHelper.camelCase(s);
    }

    public String mockParameters(Unit unit, Module module){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < unit.getParameters().length; i ++){
            if(i > 0){
                builder.append(", ");
            }
            try {
                builder.append(JSON.toJSONString(POJOMocker.mock(unit.getParameters()[i].getGenericType(), module.getInterfaceClass()),true));
            } catch (Throwable e) {
                builder.append("{}");
            }
        }
        return builder.toString();
    }
}
