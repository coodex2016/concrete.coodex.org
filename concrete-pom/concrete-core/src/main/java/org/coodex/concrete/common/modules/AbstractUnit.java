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

package org.coodex.concrete.common.modules;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by davidoff shen on 2016-11-30.
 */
//@SuppressWarnings({"rawtypes"})
public abstract class AbstractUnit<PARAM extends AbstractParam/*, MODULE extends AbstractModule*/>
        implements Annotated, Comparable<AbstractUnit<PARAM>>, Documentable {

    private static final ServiceLoader<RoleNameMapper> ROLE_NAME_MAPPER = new LazyServiceLoader<RoleNameMapper>(
            () -> role -> role
    ) {
    };

    private final boolean deprecated;
    private Method method;
    private AbstractModule<?> declaringModule;
    private DefinitionContext context;
    private List<PARAM> params = new ArrayList<>();

    public AbstractUnit(Method method, AbstractModule<?> module) {
        this.method = method;
        this.declaringModule = module;
        this.deprecated = method.getAnnotation(Deprecated.class) != null;

        for (int i = 0, j = method.getParameterTypes().length; i < j; i++) {
            params.add(buildParam(method, i));
        }
        afterInit();
    }

    protected void afterInit() {
    }

    public AbstractModule<?> getDeclaringModule() {
        return declaringModule;
    }

    public Method getMethod() {
        return method;
    }

    private Description getDesc() {
        return getDeclaredAnnotation(Description.class);
    }

    /**
     * @return 服务名称
     */
    public abstract String getName();


    protected abstract PARAM buildParam(Method method, int index);

    /**
     * @return 文档化的名称
     */
    @Override
    public String getLabel() {
        return getDesc() == null ? getName() : getDesc().name();
    }

    /**
     * @return 服务说明
     */
    @Override
    public String getDescription() {
        return getDesc() == null ? null : getDesc().description();
    }

    /**
     * @return 调用方法名
     */
    public String getFunctionName() {
        return method.getName();
    }


    /**
     * @return 调用方式
     */
    public abstract String getInvokeType();


    /**
     * @return 返回值类型
     */
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    /**
     * @return 返回值泛型类型
     */
    public Type getGenericReturnType() {
        return method.getGenericReturnType();
    }

    /**
     * @return access control list
     */
    public AccessAllow getAccessAllow() {
        return getContext().getAnnotation(AccessAllow.class);
    }

    public String[] getRoles() {
        AccessAllow accessAllow = getAccessAllow();
        return accessAllow == null ? new String[0] :
                Arrays.stream(accessAllow.roles())
                        .map(role -> ROLE_NAME_MAPPER.get().getRoleName(role))
                        .toArray(String[]::new);
    }

    /**
     * @return 获取签名信息
     */
    @SuppressWarnings("unused")
    public Signable getSignable() {
        return getContext().getAnnotation(Signable.class);
    }

    /**
     * @param annotationClass annotationClass
     * @param <T>             <T>
     * @return 获取某个注解
     */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = getDeclaredAnnotation(annotationClass);
        return annotation == null ?
                ((Annotated) declaringModule).getDeclaredAnnotation(annotationClass) :
                annotation;
    }

    @Override
    public Annotation[] getAnnotations() {
        return method.getAnnotations();
    }

    /**
     * @return 方法参数
     */
    public final PARAM[] getParameters() {
        return toArrays(params);
    }

    protected abstract PARAM[] toArrays(List<PARAM> params);

    public synchronized DefinitionContext getContext() {
        if (context == null)
            context = toContext();
        return context;
    }

    protected abstract DefinitionContext toContext();


    public boolean isDeprecated() {
        return deprecated;
    }
}
