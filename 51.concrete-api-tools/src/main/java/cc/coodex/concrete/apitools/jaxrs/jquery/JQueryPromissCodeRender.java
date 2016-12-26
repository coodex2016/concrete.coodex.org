package cc.coodex.concrete.apitools.jaxrs.jquery;

import cc.coodex.concrete.apitools.jaxrs.AbstractRender;
import cc.coodex.concrete.common.ConcreteToolkit;
import cc.coodex.concrete.jaxrs.JaxRSModuleMaker;
import cc.coodex.concrete.jaxrs.struct.Module;
import cc.coodex.concrete.jaxrs.struct.Param;
import cc.coodex.concrete.jaxrs.struct.Unit;

import java.io.IOException;
import java.util.*;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public class JQueryPromissCodeRender extends AbstractRender {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.jquery.js.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/jquery/code/v1/";


    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }

    private String buildMethod(Unit unit, Module module) {
        StringBuilder builder = new StringBuilder();
        StringBuilder parameters = new StringBuilder();
        builder.append("function (");
        for (int i = 0; i < unit.getParameters().length; i++) {
            Param param = unit.getParameters()[i];
            if (i > 0) {
                builder.append(", ");
                parameters.append(", ");
            }
            String n = param.getName();
            builder.append(n);
            parameters.append("\"").append(n).append("\": ").append(n);
        }
        builder.append(") {return invoke({\"path\": \"").append(module.getName()).append(unit.getName()).append("\",\"param\": {")
                .append(parameters.toString()).append("},\"method\": \"").append(unit.getInvokeType()).append("\" });}");
        return builder.toString();
    }

    private String overload(Module module) {
        Map<String, Set<Unit>> methods = new HashMap<String, Set<Unit>>();
        for (Unit unit : module.getUnits()) {
            String key = unit.getMethod().getName();
            Set<Unit> units = methods.get(key);
            if (units == null) {
                units = new HashSet<Unit>();
                methods.put(key, units);
            }

            units.add(unit);
        }

        StringBuilder builder = new StringBuilder();
        for (String methodName : methods.keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("\"").append(methodName).append("\": ");

            Unit[] units = methods.get(methodName).toArray(new Unit[0]);
            if (units.length > 1) {
                // overload
                builder.append("overload(\"").append(methodName).append("\", {");
                for (int i = 0; i < units.length; i++) {
                    if (i > 0) builder.append(", ");
                    builder.append("\"").append(units[i].getParameters().length).append("\": ")
                            .append(buildMethod(units[i], module));
                }
                builder.append("})");
            } else {
                builder.append(buildMethod(units[0], module));
            }
        }

        return builder.toString();
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        List<Module> moduleList = ConcreteToolkit.loadModules(RENDER_NAME, packages);
        Set<String> modules = new HashSet<String>();
        for (Module module : moduleList) {
            StringBuilder builder = new StringBuilder();
            builder.append("register(\"").append(module.getInterfaceClass().getSimpleName()).append("\", \"")
                    .append(module.getInterfaceClass().getPackage().getName()).append("\", { ")
                    .append(overload(module))
                    .append("});");
            modules.add(builder.toString());
        }
        writeTo("jquery-concrete.js", "jquery-concrete.js.ftl", "modules", modules);
    }
}
