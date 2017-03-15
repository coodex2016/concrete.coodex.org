package org.coodex.practice.jaxrs.jersey;

import org.coodex.concrete.attachments.server.DownloadResource;
import org.coodex.concrete.attachments.server.UploadByFormResource;
import org.coodex.concrete.jaxrs.ConcreteExceptionMapper;
import org.coodex.concrete.jaxrs.CreatedByConcrete;
import org.coodex.concrete.jaxrs.JaxRSServiceHelper;
import org.coodex.concrete.support.jsr339.javassist.JSR339ClassGenerator;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-11-28.
 */
public class ExampleApplication extends ResourceConfig {

    private final static Logger log = LoggerFactory.getLogger(ExampleApplication.class);
    private static final String GENERATOR_NAME = JSR339ClassGenerator.GENERATOR_NAME;

    public ExampleApplication() {
//        super(JacksonFeature.class);
//        super(JSR339ClassGenerator.generates("org.coodex.practice.jarxs.api"));
        registerClasses(JacksonFeature.class, LoggingFeature.class, ConcreteExceptionMapper.class,
                DownloadResource.class, UploadByFormResource.class);
        Set<Class<?>> classes = JaxRSServiceHelper.generate(GENERATOR_NAME, "org.coodex.practice.jaxrs.api", "org.coodex.concrete.attachments");
//        registerClasses(JaxRSHelper.generate(GENERATOR_NAME, "org.coodex.practice.jaxrs.api"));

        log.debug("{} classes created.", classes.size());
        for (Class<?> clz : classes) {
            log.debug("class: {}, Annotation:{}", clz, Arrays.deepToString(clz.getAnnotations()));
            for (Method method : clz.getDeclaredMethods()) {
                log.debug("method: {}, Annotations:{}\nparam: {}", method,
                        Arrays.deepToString(method.getAnnotations()),
                        Arrays.deepToString(method.getParameterAnnotations()));
            }
        }
        registerClasses(classes);
    }

    //
    public static void main(String[] args) {
        Set<Class<?>> classes = JaxRSServiceHelper.generate(GENERATOR_NAME, "org.coodex.practice.jaxrs.api");
        log.debug("{} classes created.", classes.size());
        for (Class<?> clz : classes) {
            log.debug("class: {}, Annotation:{}", clz, clz.getAnnotations());
            for (Method method : clz.getMethods()) {
                if (method.getAnnotation(CreatedByConcrete.class) != null) {
                    log.debug("method: {}, Annotations: {}", method.toGenericString(), method.getAnnotations());
                    log.debug("param: {}", Arrays.deepToString(method.getParameterAnnotations()));
                }
            }
        }
    }

}
