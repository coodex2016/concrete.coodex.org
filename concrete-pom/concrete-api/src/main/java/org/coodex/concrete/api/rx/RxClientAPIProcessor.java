/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.api.rx;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes("org.coodex.concrete.api.ConcreteService")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RxClientAPIProcessor extends AbstractProcessor {

    private static final String[] PRIMITIVE_TYPES = {
            boolean.class.getName(),
            byte.class.getName(),
            short.class.getName(),
            int.class.getName(),
            long.class.getName(),
            char.class.getName(),
            float.class.getName(),
            double.class.getName()
    };

    private static final String[] AUTO_BOXED_TYPES = {
            Boolean.class.getName(),
            Byte.class.getName(),
            Short.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Character.class.getName(),
            Float.class.getName(),
            Double.class.getName()
    };

    private static Set<Name> processedClasses = new HashSet<>();

    //    private Singleton<AcceptableServiceLoader<String, RxCodeBuilder>> rxCodeBuilderLoaderSingleton =
//            new Singleton<>(() -> new AcceptableServiceLoader<String, RxCodeBuilder>() {
//            });
//    private static Singleton<ServiceLoader<RxCodeBuilder>> serviceLoaderSingleton = new Singleton<>(new Singleton.Builder<ServiceLoader<RxCodeBuilder>>() {
//        @Override
//        public ServiceLoader<RxCodeBuilder> build() {
//            return ServiceLoader.load(RxCodeBuilder.class);
//        }
//    });
    private static SingletonMap<String, RxCodeBuilder> builderSingletonMap = new SingletonMap<>(new SingletonMap.Builder<String, RxCodeBuilder>() {
        @Override
        public RxCodeBuilder build(String key) {
            try {
                Class clazz = Class.forName(key);
                return (RxCodeBuilder) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                return null;
            }
        }
    });
    private static Class[] mirrorClasses = new Class[]{
            ArrayType.class,
            DeclaredType.class,
            ErrorType.class,
            ExecutableType.class,
            IntersectionType.class,
            NoType.class,
            NullType.class,
            PrimitiveType.class,
            ReferenceType.class,
            TypeVariable.class,
            UnionType.class,
            WildcardType.class
    };

    private static String autoBox(PrimitiveType type) {
        return AUTO_BOXED_TYPES[Common.findInArray(type.toString(), PRIMITIVE_TYPES)];
    }

    private static String getActualType(TypeMirror typeMirror, TypeElement context, Types typesUtil, boolean autoBoxed) {
        DeclaredType containing = (DeclaredType) context.asType();
        // 变量类型
        if (typeMirror instanceof TypeVariable) {
            return typesUtil.asMemberOf(containing, ((TypeVariable) typeMirror).asElement()).toString();
        }

        // 私有类型
        if (typeMirror instanceof PrimitiveType) {
            return autoBoxed ? autoBox((PrimitiveType) typeMirror) : typeMirror.toString();
        }

        // 数组类型
        if (typeMirror instanceof ArrayType) {
            return getActualType(((ArrayType) typeMirror).getComponentType(), context, typesUtil, false) + "[]";
        }

        // void
        if (typeMirror instanceof NoType) {
            return Void.class.getName();
        }

        if (typeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            if (declaredType.getTypeArguments().size() > 0) {
                StringBuilder builder = new StringBuilder(declaredType.asElement().toString());
                builder.append("<");
                int i = 0;
                for (TypeMirror p : declaredType.getTypeArguments()) {
                    if (i++ > 0) builder.append(", ");
                    builder.append(getActualType(p, context, typesUtil, false));
                }
                builder.append(">");
                return builder.toString();
            } else {
                return declaredType.toString();
            }
        }
        return null;
    }

    private static RxCodeBuilder getRxCodeBuilder(String rxType) {
        return builderSingletonMap.get(rxType);
//        final Set<RxCodeBuilder> rxCodeBuilders = new HashSet<>();
//        serviceLoaderSingleton.get().forEach((builder) -> {
//            if (builder.accept(rxType)) {
//                rxCodeBuilders.add(builder);
//            }
//        });
//        return rxCodeBuilders.size() > 0 ? rxCodeBuilders.iterator().next() : null;
    }

    private static String getNames(AnnotatedConstruct construct, Class[] elementClasses) {
        StringBuilder builder = new StringBuilder();
        boolean blank = true;
        for (Class e : elementClasses) {
            if (e.isAssignableFrom(construct.getClass())) {
                if (!blank) {
                    builder.append(", ");
                }
                builder.append(e.getSimpleName());
                blank = false;
            }
        }
        return builder.toString();
    }

    private static String getMirrorType(TypeMirror typeMirror) {
        return getNames(typeMirror, mirrorClasses);
    }

    private static Set<String> getBuilders() {
        Set<String> result = new HashSet<>();
        result.add(CompletableCodeBuilder.class.getName());
        try {
            Enumeration<URL> urlEnumeration = RxClientAPIProcessor.class.getClassLoader()
                    .getResources("META-INF/services/org.coodex.concrete.api.rx.RxCodeBuilder");
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String className = line.trim();
                        if (!Common.isBlank(className) && !className.startsWith("#"))
                            result.add(className);
                    }
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Types typesUtil = processingEnv.getTypeUtils();
        Elements elementsUtil = processingEnv.getElementUtils();
        TypeMirror objectType = elementsUtil.getTypeElement(Object.class.getName()).asType();


        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ConcreteService.class)) {

            if (!(annotatedElement instanceof TypeElement)) continue;
            TypeElement serviceType = (TypeElement) annotatedElement;
            if (!(serviceType.asType() instanceof DeclaredType)) continue;
            ConcreteService serviceAnnotation = annotatedElement.getAnnotation(ConcreteService.class);
            if (serviceAnnotation.nonspecific() || serviceAnnotation.notService()) continue;
            if (processedClasses.contains(serviceType.getQualifiedName())) continue;

            PackageElement packageElement = elementsUtil.getPackageOf(serviceType);

//            for (String rxType : serviceAnnotation.reactiveBuilder()) {
//            String[] rxBuilders = serviceAnnotation.reactiveBuilder();
//            for (int x = -1; x < rxBuilders.length; x++) {
//                String rxType = x == -1 ? CompletableCodeBuilder.class.getName() : rxBuilders[x];
            for (String rxType : getBuilders()) {
                RxCodeBuilder builder = getRxCodeBuilder(rxType);
                if (builder == null) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.WARNING,
                            "no code builder for: " + rxType,
                            serviceType);
                    continue;
                }
                // build type
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "build " + rxType + " interface.");
//                buildRxCode(serviceType, builder);
                final StringBuilder codeBuilder = new StringBuilder();
                String packageName = packageElement.isUnnamed() ? null : packageElement.toString();

                codeBuilder.append(packageName == null ? "" : ("package " + packageName + ";")).append("\n\n")
                        .append("@").append(ReactiveExtensionFor.class.getName()).append("(")
                        .append(serviceType.getQualifiedName()).append(".class)\n")
                        .append("public interface ").append(builder.getAdj()).append(serviceType.getSimpleName()).append("{\n");
                // methods
                elementsUtil.getAllMembers(serviceType).forEach((memberElement) -> {
                    if (!(memberElement instanceof ExecutableElement)) return;
                    ExecutableElement e = (ExecutableElement) memberElement;
                    if (e.getEnclosingElement().asType().equals(objectType)) return;
                    codeBuilder.append("\t")
                            .append(builder.getReturnTypeCode(getActualType(e.getReturnType(), serviceType, typesUtil, true)))
                            .append(" ").append(e.getSimpleName()).append('(');

                    int i = 0;
                    for (VariableElement param : e.getParameters()) {
                        if (i++ != 0) codeBuilder.append(", ");
                        codeBuilder.append(getActualType(param.asType(), serviceType, typesUtil, false))
                                .append(" ").append(param.getSimpleName());
                    }

                    codeBuilder.append(");\n\n");

                });
                codeBuilder.append("}");
                // add code;

                String fileName = (packageName == null ? "" : (packageName + ".")) + builder.getAdj() + serviceType.getSimpleName();
                String code = codeBuilder.toString();
                try {
                    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(fileName);
                    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                        out.println(code);
                    }
                } catch (Throwable th) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                            "build " + rxType + " interface failed: " + th.getLocalizedMessage() + "\n\n" + code);
                }
            }
            processedClasses.add(serviceType.getQualifiedName());

        }
        return false;
    }
}
