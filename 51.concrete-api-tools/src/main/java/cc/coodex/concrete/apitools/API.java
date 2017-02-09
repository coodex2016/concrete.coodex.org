package cc.coodex.concrete.apitools;

import cc.coodex.concrete.common.ConcreteSPIFacade;
import cc.coodex.util.SPIFacade;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-12-01.
 */
public class API {
    private static final SPIFacade<ConcreteAPIRender> RENDERS = new ConcreteSPIFacade<ConcreteAPIRender>() {
    };

    public static void generate(String desc, String path, String... packages) throws IOException {
        if (RENDERS.getAllInstances().size() == 0)
            throw new RuntimeException("NONE render found.");
        for (ConcreteAPIRender render : RENDERS.getAllInstances()) {
            synchronized (render) {
                if (render.isAccept(desc)) {
                    render.setRoot(path);
                    render.writeTo(packages);
                    return;
                }
            }
        }

        throw new RuntimeException("NONE render for " + desc + " found.");
    }


}
