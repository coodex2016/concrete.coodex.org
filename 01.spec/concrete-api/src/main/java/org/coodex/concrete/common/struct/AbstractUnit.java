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

package org.coodex.concrete.common.struct;

import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.common.DefinitionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class AbstractUnit<PARAM extends AbstractParam, MODULE extends AbstractModule> implements Annotated, Comparable<AbstractUnit> {

    private Method method;
    private MODULE declaringModule;
    private DefinitionContext context;
    private final boolean deprecated;
    private List<PARAM> params = new ArrayList<PARAM>();

    public AbstractUnit(Method method, MODULE module) {
        this.method = method;
        this.declaringModule = module;
        this.deprecated = method.getAnnotation(Deprecated.class) != null;

        for(int i = 0, j = method.getParameterTypes().length; i < j; i ++){
            params.add(buildParam(method, i));
        }
        afterInit();
    }

    protected void afterInit(){}

    public MODULE getDeclaringModule() {
        return declaringModule;
    }

    public Method getMethod() {
        return method;
    }

    private Description getDesc() {
        return getDeclaredAnnotation(Description.class);
    }

    /**
     * 服务名称
     *
     * @return
     */
    public abstract String getName();


    protected abstract PARAM buildParam(Method method, int index);

    /**
     * 文档化的名称
     *
     * @return
     */
    public String getLabel() {
        return getDesc() == null ? getName() : getDesc().name();
    }

    /**
     * 服务说明
     *
     * @return
     */
    public String getDescription() {
        return getDesc() == null ? null : getDesc().description();
    }

    /**
     * 调用方法名
     *
     * @return
     */
    public String getFunctionName() {
        return method.getName();
    }


    /**
     * 调用方式
     *
     * @return
     */
    public abstract String getInvokeType();


    /**
     * 返回值类型
     *
     * @return
     */
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    /**
     * 返回值泛型类型
     *
     * @return
     */
    public Type getGenericReturnType() {
        return method.getGenericReturnType();
    }

    /**
     * access control list
     *
     * @return
     */
    public AccessAllow getAccessAllow() {
        return getContext().getAnnotation(AccessAllow.class);
    }

    /**
     * 获取签名信息
     *
     * @return
     */
    public Signable getSignable() {
        return getContext().getAnnotation(Signable.class);
    }

    /**
     * 获取某个注解
     *
     * @param annotationClass
     * @param <T>
     * @return
     */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = getDeclaredAnnotation(annotationClass);
        return annotation == null ?
                (T) declaringModule.getAnnotation(annotationClass) :
                annotation;
    }

    @Override
    public Annotation[] getAnnotations() {
        return method.getAnnotations();
    }

    /**
     * 方法参数
     *
     * @return
     */
    public final PARAM[] getParameters(){
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
