/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.spring.components;

import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.coodex.concrete.Client;
import org.coodex.concrete.ConcreteClient;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.id.IDGenerator;
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.ClientHelper.isConcreteService;
import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.ctClassToClass;

@SuppressWarnings("unused")
@Named
public class ConcreteClientBeanPostProcessor
        extends AbstractInjectableBeanPostProcessor<ConcreteClientBeanPostProcessor.ConcreteClientInjectKey> {
    public static class ConcreteClientInjectKey implements InjectInfoKey {
        private final ConcreteClient concreteClient;
        private final Class<?> concreteClass;

        public ConcreteClientInjectKey(ConcreteClient concreteClient, Class<?> concreteClass) {
            this.concreteClient = concreteClient;
            this.concreteClass = concreteClass;
        }

        public ConcreteClient getConcreteClient() {
            return concreteClient;
        }

        public Class<?> getConcreteClass() {
            return concreteClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConcreteClientInjectKey that = (ConcreteClientInjectKey) o;
            return Objects.equals(concreteClient, that.concreteClient) && Objects.equals(concreteClass, that.concreteClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(concreteClient, concreteClass);
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(ConcreteClientBeanPostProcessor.class);

    private final static String GET_INSTANCE = "__getConcreteServiceInstance";
    //    private final Set<String> registered = new HashSet<>();
//    private final Map<String, Integer> moduleMap = new HashMap<>();
    private final AtomicInteger atomicInteger = new AtomicInteger(1);
//    @Inject
//    private DefaultListableBeanFactory defaultListableBeanFactory;

//    private void scan(java.lang.annotation.Annotation[][] annotations, Class<?>[] parameters) {
//        for (int i = 0; i < annotations.length; i++) {
//            if (annotations[i] == null) {
//                continue;
//            }
//            for (java.lang.annotation.Annotation annotation : annotations[i]) {
//                if (annotation instanceof ConcreteClient) {
//                    register(parameters[i], (ConcreteClient) annotation);
//                }
//            }
//        }
//    }

    @Override
    protected boolean accept(Annotated annotated) {
        Class<?> c = GenericTypeHelper.typeToClass(annotated.getReferenceType());
        return c != null && isConcreteService(c)
                && annotated.getAnnotation(ConcreteClient.class) != null;
    }

    @Override
    protected Class<?> getInjectClass(ConcreteClientInjectKey key, Class<?> beanClass) {
        ClassPool classPool = ClassPool.getDefault();
        String className = key.getConcreteClass().getName();
        String newClassName = className + "$CBC$" + String.format("%08d", atomicInteger.getAndIncrement());
        try {
            return createClass(key.getConcreteClass(), key.getConcreteClient(), classPool, className, newClassName);
        } catch (CannotCompileException e) {
            throw Common.rte(e);
        }
    }

    @Override
    protected String newBeanName() {
        return "concreteClient_" + IDGenerator.newId();
    }

    @Override
    protected ConcreteClientInjectKey getKey(Annotated annotated) {
        return new ConcreteClientInjectKey(annotated.getAnnotation(ConcreteClient.class), GenericTypeHelper.typeToClass(annotated.getReferenceType()));
    }

    @Override
    @Deprecated
    protected boolean injectNoneAnnotation() {
        return true;
    }

    //    @Override
//    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//
//        scanAndRegisterClientBean(beanClass);
//
//        return null;
//    }

//    private void scanAndRegisterClientBean(Class<?> beanClass) {
//        for (Constructor<?> constructor : beanClass.getConstructors()) {
//            scan(constructor.getParameterAnnotations(), constructor.getParameterTypes());
//        }
//
//        for (Method method : beanClass.getMethods()) {
//            scan(method.getParameterAnnotations(), method.getParameterTypes());
//        }
//
//        for (Field field : ReflectHelper.getAllDeclaredFields(beanClass)) {
//            ConcreteClient concreteClient = field.getAnnotation(ConcreteClient.class);
//            if (concreteClient != null) {
//                register(field.getType(), concreteClient);
//            }
//        }
//    }

//    @Override
//    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
//        scanAndRegisterClientBean(bean.getClass());
//        return true;
//    }


//    private Integer getModuleIndex(String v) {
//        String hash = Common.sha1(v);
//        if (!moduleMap.containsKey(hash)) {
//            moduleMap.put(hash, atomicInteger.getAndIncrement());
//        }
//        return moduleMap.get(hash);
//    }


    private Annotation concreteClient(ConcreteClient concreteClient, ConstPool constPool) {
        Annotation annotation = new Annotation(ConcreteClient.class.getName(), constPool);
        annotation.addMemberValue("value", new StringMemberValue(concreteClient.value(), constPool));
        return annotation;
    }

    private CtClass[] getParameterTypes(Method method, ClassPool classPool) {
        List<CtClass> list = new ArrayList<>();
        for (Class<?> c : method.getParameterTypes()) {
//            list.add(classPool.getOrNull(c.getName()));
            list.add(JavassistHelper.getCtClass(c, classPool));
        }
        return list.toArray(new CtClass[0]);
    }


//    private synchronized void register(Class<?> concreteService, ConcreteClient concreteClient) {
//
//
//        try {
//            IF.not(isConcreteService(concreteService),
//                    concreteService + "is NOT ConcreteService.");
//            ClassPool classPool = ClassPool.getDefault();
//            String className = concreteService.getName();
//            String newClassName = className + "$CBC$" + getModuleIndex(concreteClient.value());
//
//            if (registered.contains(newClassName)) {
//                return;
//            }
//            Class<?> generated = createClass(concreteService, concreteClient, classPool, className, newClassName);
////                    Common.isJava9AndLast() ?
////                    ctClass.toClass(concreteService) :
////                    ctClass.toClass();
//
//            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(generated);
//            defaultListableBeanFactory.registerBeanDefinition(
//                    CREATE_BY_CONCRETE + Common.sha1(newClassName), beanDefinitionBuilder.getBeanDefinition());
//            registered.add(newClassName);
//            log.info("ConcreteClient Bean[className: {}, module: {}] registered.",
//                    newClassName, concreteClient.value()
//            );
//
//        } catch (Throwable throwable) {
//            throw Common.rte(throwable);
//        }
//
//
//    }

    private Class<?> createClass(Class<?> concreteService, ConcreteClient concreteClient, ClassPool classPool, String className, String newClassName) throws CannotCompileException {
        CtClass ctClass = classPool.makeClass(newClassName);

        ctClass.setInterfaces(new CtClass[]{
//                    classPool.getOrNull(concreteService.getName())
                JavassistHelper.getCtClass(concreteService, classPool)
        });
        ClassFile classFile = ctClass.getClassFile();
        ConstPool constPool = classFile.getConstPool();
        classFile.setVersionToJava5();
        classFile.addAttribute(
                JavassistHelper.aggregate(constPool, concreteClient(concreteClient, constPool))
        );

        // 1，添加方法，私有，__getConcreteServiceInstance
        CtMethod ctMethod = new CtMethod(
//                    classPool.getOrNull(concreteService.getName()),
                JavassistHelper.getCtClass(concreteService, classPool),
                GET_INSTANCE,
                new CtClass[0],
                ctClass
        );
        ctMethod.setModifiers(Modifier.PRIVATE);
        String moduleForSrc = Common.isBlank(concreteClient.value()) ? "null" : ("\"" + concreteClient.value() + "\"");
        ctMethod.setBody("return " + Client.class.getName() + ".getInstance(" +
                className + ".class, " + moduleForSrc + ");");
        ctClass.addMethod(ctMethod);

        // 2,增加接口实现
        for (Method method : concreteService.getMethods()) {
            Class<?> returnType = method.getReturnType();
            ctMethod = new CtMethod(
//                        classPool.getOrNull(returnType.getName()),
                    JavassistHelper.getCtClass(returnType, classPool),
                    method.getName(),
                    getParameterTypes(method, classPool),
                    ctClass
            );
            StringBuilder builder = new StringBuilder();
            if (!void.class.equals(returnType)) {
                builder.append("return ");
            }
            builder.append(GET_INSTANCE).append("().").append(method.getName()).append("(");
            if (method.getParameterTypes().length > 0) {
                builder.append("$$");
            }
            builder.append(");");
            ctMethod.setBody(builder.toString());
            ctClass.addMethod(ctMethod);
        }

        return ctClassToClass(ctClass, concreteService);
    }


}
