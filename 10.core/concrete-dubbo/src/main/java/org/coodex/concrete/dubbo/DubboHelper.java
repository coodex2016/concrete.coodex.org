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

package org.coodex.concrete.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.util.Common;
import org.coodex.util.GenericType;
import org.coodex.util.SingletonMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.dubbo.common.bytecode.ClassGenerator.getClassPool;

public class DubboHelper {

    public static final SingletonMap<String, ApplicationConfig> applications =
            new SingletonMap<String, ApplicationConfig>(
                    new SingletonMap.Builder<String, ApplicationConfig>() {
                        @Override
                        public ApplicationConfig build(String key) {
                            return new ApplicationConfig(key);
                        }
                    }
            );

    public static final String SUBJOIN = "subjoin";
    public static final String AGENT = "user-agent";
    public static final String RESULT = "result";

    private static SignatureAttribute.Type returnType = JavassistHelper.classType(
            new GenericType<Map<String, String>>() {
            }.genericType(),
            DubboHelper.class
    );

    private static SingletonMap<Class, Class> dubboClasses =
            new SingletonMap<Class, Class>(new SingletonMap.Builder<Class, Class>() {
                @Override
                public Class build(Class key) {
                    try {
                        String newClassName = key.getName() + "$DP";
                        ClassPool classPool = getClassPool(key.getClassLoader());
                        CtClass ctClass = classPool.makeInterface(newClassName);
                        ClassFile classFile = ctClass.getClassFile();
                        ConstPool constPool = classFile.getConstPool();

                        classFile.addAttribute(proxyFor(key, constPool));

                        classFile.setVersionToJava5();
                        for (Method method : key.getMethods()) {
                            CtMethod ctMethod = new CtMethod(
                                    classPool.getCtClass(Map.class.getName()),
                                    method.getName(),
                                    getParameterTypes(method.getParameterTypes()),
                                    ctClass
                            );
                            ctMethod.setGenericSignature(new SignatureAttribute.MethodSignature(
                                    null,
                                    getGenericParametersType(key, method),
                                    returnType,
                                    null
                            ).encode());
                            ctClass.addMethod(ctMethod);
                        }
                        return ctClass.toClass(key.getClassLoader(), key.getProtectionDomain());
                    } catch (Throwable th) {
                        throw th instanceof RuntimeException ?
                                (RuntimeException) th :
                                new RuntimeException(th.getLocalizedMessage(), th);
                    }
                }
            });
    private static SingletonMap<String, RegistryConfig> registryConfigs =
            new SingletonMap<String, RegistryConfig>(new SingletonMap.Builder<String, RegistryConfig>() {
                @Override
                public RegistryConfig build(String key) {
                    try {
                        URI uri = new URI(key);
                        RegistryConfig registryConfig = new RegistryConfig();
                        registryConfig.setProtocol(uri.getScheme());
                        registryConfig.setAddress(uri.getHost());
                        registryConfig.setPort(uri.getPort());

                        String userInfo = uri.getUserInfo();
                        if (!Common.isBlank(userInfo)) {
                            int index = userInfo.indexOf(':');
                            if (index > 0) {
                                registryConfig.setUsername(userInfo.substring(0, index));
                                registryConfig.setPassword(userInfo.substring(index + 1));
                            } else {
                                registryConfig.setUsername(userInfo);
                            }
                        }
                        return registryConfig;
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e.getLocalizedMessage(), e);
                    }

                }
            });

    private static CtClass[] getParameterTypes(Class<?>[] parameterTypes) throws NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        List<CtClass> ctClasses = new ArrayList<CtClass>();
        for (Class clz : parameterTypes) {
            ctClasses.add(classPool.getCtClass(clz.getName()));
        }
        return ctClasses.toArray(new CtClass[0]);
    }

    private static SignatureAttribute.Type[] getGenericParametersType(Class clz, Method method) {
        List<SignatureAttribute.Type> types = new ArrayList<SignatureAttribute.Type>();
        for (Type type : method.getGenericParameterTypes()) {
            types.add(JavassistHelper.classType(type, clz));
        }
        return types.toArray(new SignatureAttribute.Type[0]);
    }

    private static AttributeInfo proxyFor(Class clz, ConstPool constPool) {
        Annotation annotation = new Annotation(ProxyFor.class.getName(), constPool);
        annotation.addMemberValue("value", new ClassMemberValue(clz.getName(), constPool));
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool,
                AnnotationsAttribute.visibleTag);
        attr.addAnnotation(annotation);
        return attr;
    }

    public static Class getDubboInterface(Class concreteService) {
        return dubboClasses.getInstance(concreteService);
    }

    public static RegistryConfig buildRegistryConfig(String spec) {
        return registryConfigs.getInstance(spec);
    }

    public static List<RegistryConfig> buildRegistryConfigs(String[] specs) {
        List<RegistryConfig> configs = new ArrayList<RegistryConfig>();
        for (String spec : specs) {
            configs.add(buildRegistryConfig(spec));
        }
        return configs;
    }

}
