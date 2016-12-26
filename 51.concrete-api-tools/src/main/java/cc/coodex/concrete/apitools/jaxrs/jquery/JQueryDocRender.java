package cc.coodex.concrete.apitools.jaxrs.jquery;

import cc.coodex.concrete.apitools.jaxrs.AbstractRender;
import cc.coodex.concrete.apitools.jaxrs.DocToolkit;
import cc.coodex.concrete.common.ConcreteToolkit;
import cc.coodex.concrete.jaxrs.JaxRSModuleMaker;
import cc.coodex.concrete.jaxrs.struct.Module;

import java.io.IOException;
import java.util.List;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class JQueryDocRender extends AbstractRender {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".doc.jquery.gitbook.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/jquery/doc/v1/";

    private DocToolkit toolkit = new JQueryDocToolkit(this);

    private void writeSummary(List<Module> modules) throws IOException {
        writeTo("SUMMARY.md",
                "SUMMARY.md",
                "modules", modules, toolkit);
    }

    private void writeModuleList(List<Module> modules) throws IOException {
        writeTo("moduleList.md",
                "moduleList.md",
                "modules", modules, toolkit);
    }

    private void writeModule(Module module) throws IOException {
        writeTo("modules" + FS + toolkit.canonicalName(module.getName()) + ".md",
                "module.md",
                "module", module, toolkit);
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        List<Module> modules = ConcreteToolkit.loadModules(RENDER_NAME, packages);



        // book.json
        if (!exists("book.json"))
            copyTo("book.json", "book.json");

        // README.md
        if (!exists("README.md"))
            copyTo("README.md", "README.md");


        // moduleList.md
        writeModuleList(modules);

        // modules
        for (Module module : modules) {
            writeModule(module);
        }

        // SUMMARY.MD
        writeSummary(modules);
    }

    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }
}
