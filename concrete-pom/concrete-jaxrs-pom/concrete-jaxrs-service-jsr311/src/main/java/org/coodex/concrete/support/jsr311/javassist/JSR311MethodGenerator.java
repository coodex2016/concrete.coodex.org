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

package org.coodex.concrete.support.jsr311.javassist;

import javassist.CtClass;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SignatureAttribute;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.concrete.support.jaxrs.javassist.AbstractMethodGenerator;
import org.coodex.concrete.support.jaxrs.javassist.CGContext;

import javax.ws.rs.core.Response;


/**
 * Created by davidoff shen on 2016-11-26.
 */
public class JSR311MethodGenerator extends AbstractMethodGenerator {

    public JSR311MethodGenerator(CGContext context, JaxrsUnit unit) {
        super(context, unit);
    }


    @Override
    protected CtClass[] getParameterTypes(Class<?> pojoClass) {
        // 增加String tokenId
        return getParameterTypesWith(pojoClass,
//                CGContext.CLASS_POOL.getOrNull(String.class.getName())
                JavassistHelper.getCtClass(String.class, CGContext.CLASS_POOL)
        );
    }

    @Override
    protected SignatureAttribute.Type[] getSignatureTypes(Class<?> pojoClass) {
        // 增加String tokenId
        return getSignatureTypesWith(pojoClass, JavassistHelper.classType(String.class, getContext().getServiceClass()));
    }


    @Override
    protected String getMethodBody(Class<?> pojoClass) {
//        String paramListSrc = getParamListSrc(1);
        String paramListSrc = getParamListSrc(pojoClass, 2);
        return "{return ($r)execute(\"" + getUnit().getFunctionName() + "\", $1"
                + (paramListSrc.length() == 0 ? ", null" : (", new java.lang.Object[]{" + paramListSrc + "}")) + ");}";
    }

    @Override
    protected SignatureAttribute.Type getReturnSignatureType() {
        return JavassistHelper.classType(Response.class, getContext().getServiceClass());
    }

    @Override
    protected CtClass getReturnType() {
//        return CGContext.CLASS_POOL.getOrNull(Response.class.getName());
        return JavassistHelper.getCtClass(Response.class, CGContext.CLASS_POOL);
    }


    @Override
    protected AttributeInfo getParameterAnnotationsAttribute() {
        return getParameterAnnotationsAttributeWith(getContext().tokenParam());
    }

    @Override
    protected String[] getContentType() {
        return null;
    }

}
