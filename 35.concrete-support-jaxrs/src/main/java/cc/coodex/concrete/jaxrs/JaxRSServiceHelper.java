package cc.coodex.concrete.jaxrs;

import cc.coodex.concrete.common.*;
import cc.coodex.concrete.jaxrs.struct.Module;
import cc.coodex.util.ClassFilter;
import cc.coodex.util.ReflectHelper;

import java.util.*;

import static cc.coodex.util.ReflectHelper.foreachClass;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JaxRSServiceHelper {

    private static final ConcreteSPIFacade<ClassGenerator> CLASS_GENERATORS = new ConcreteSPIFacade<ClassGenerator>() {
    };

    private static ClassGenerator getGenerator(String desc) {
        for (ClassGenerator classGenerator : CLASS_GENERATORS.getAllInstances()) {
            if (classGenerator.isAccept(desc))
                return classGenerator;
        }
        throw new RuntimeException("no class generator found for " + desc + ".");
    }



    public static Set<Class<?>> generate(String desc, String... packages) {

        Set<Class<?>> classes = new HashSet<Class<?>>();
        ClassGenerator classGenerator = getGenerator(desc);

        registErrorCodes(packages);

        List<Module> modules = ConcreteToolkit.loadModules(desc, packages);

        try {
            for (Module module : modules) {
                classes.add(classGenerator.generatesImplClass(module));
            }

            return classes;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    private static final ClassFilter CONCRETE_ERROR = new ClassFilter() {
        @Override
        public boolean accept(Class<?> clazz) {
            return clazz != null
                    && AbstractErrorCodes.class.isAssignableFrom(clazz);
        }
    };


    public static void foreachErrorClass(ReflectHelper.Processer processor, String... packages) {
        foreachClass(processor, CONCRETE_ERROR, packages);
    }

    @SuppressWarnings("unchecked")
    private static void registErrorCodes(String[] packages) {
        ErrorMessageFacade.register(AbstractErrorCodes.class, ErrorCodes.class);

        foreachErrorClass(new ReflectHelper.Processer() {
            @Override
            public void process(Class<?> serviceClass) {
                if (AbstractErrorCodes.class.isAssignableFrom(serviceClass))
                    ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) serviceClass);
            }
        }, packages);
    }


    @SuppressWarnings("unchecked")
    public static List<ErrorDefinition> getAllErrorInfo(String... packages) {
        final List<ErrorDefinition> errorDefinitions = new ArrayList<ErrorDefinition>();
        registErrorCodes(packages);
        for (Integer i : ErrorMessageFacade.allRegisteredErrorCodes()) {
            errorDefinitions.add(new ErrorDefinition(i.intValue()));
        }
        Collections.sort(errorDefinitions);
        return errorDefinitions;
    }
}
