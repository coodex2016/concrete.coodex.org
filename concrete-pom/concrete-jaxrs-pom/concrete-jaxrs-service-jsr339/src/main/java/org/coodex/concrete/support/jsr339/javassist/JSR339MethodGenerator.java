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

package org.coodex.concrete.support.jsr339.javassist;

import javassist.CtClass;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.concrete.support.jaxrs.javassist.AbstractMethodGenerator;
import org.coodex.concrete.support.jaxrs.javassist.CGContext;
import org.coodex.concrete.support.jsr339.JSR339Common;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

/**
 * Created by davidoff shen on 2016-11-27.
 */
public class JSR339MethodGenerator extends AbstractMethodGenerator {

//    private final static Logger log = LoggerFactory.getLogger(JSR339MethodGenerator.class);


    public JSR339MethodGenerator(CGContext context, JaxrsUnit unit) {
        super(context, unit);
    }

    @Override
    protected CtClass[] getParameterTypes(Class<?> pojoClass) {
        // 参数1：@Suspended AsyncResponse
        // 参数2: @CookieParam String tokenId
        return getParameterTypesWith(
                pojoClass,
//                CGContext.CLASS_POOL.getOrNull(AsyncResponse.class.getName()),
                JavassistHelper.getCtClass(AsyncResponse.class, CGContext.CLASS_POOL),
//                CGContext.CLASS_POOL.getOrNull(String.class.getName())
                JavassistHelper.getCtClass(String.class, CGContext.CLASS_POOL)
        );
//        Param[] params = getUnit().getParameters();
//        CtClass[] parameters = new CtClass[params.length + 1];
//
//        parameters[0] = CGContext.CLASS_POOL.getOrNull(AsyncResponse.class.getName());
//
//        for (int i = 0; i < params.length; i++) {
//            parameters[i + 1] = CGContext.CLASS_POOL
//                    .getOrNull(JavassistHelper.getTypeName(params[i].getType()));
//        }
//        return parameters;
    }

    @Override
    protected SignatureAttribute.Type[] getSignatureTypes(Class<?> pojoClass) {
        // 参数1：@Suspended AsyncResponse
        // 参数2: @CookieParam String tokenId
        return getSignatureTypesWith(
                pojoClass,
                new SignatureAttribute.ClassType(AsyncResponse.class.getName()),
                new SignatureAttribute.ClassType(String.class.getName()));
//        Param[] params = getUnit().getParameters();
//        SignatureAttribute.Type[] parameters = new SignatureAttribute.Type[params.length + 1];
//
//        parameters[0] = new SignatureAttribute.ClassType(AsyncResponse.class.getName());
//
//        for (int i = 0; i < params.length; i++) {
//            parameters[i + 1] = JavassistHelper.classType(params[i].getGenericType(), getContext().getServiceClass());
//        }
//        return parameters;
    }

    @Override
    protected String getMethodBody(Class<?> pojoClass) {
        String paramListSrc = getParamListSrc(pojoClass, 3);
        return "{execute(\"" + getUnit().getFunctionName() + "\", $1, $2"
                + (paramListSrc.length() == 0 ? ", null" : (", new java.lang.Object[]{" + paramListSrc + "}")) + "); return null;}";
    }

    @Override
    protected SignatureAttribute.Type getReturnSignatureType() {
        return new SignatureAttribute.BaseType(void.class.getName());
    }

    @Override
    protected CtClass getReturnType() {
//        return CGContext.CLASS_POOL.getOrNull(void.class.getName());
        return JavassistHelper.getCtClass(void.class, CGContext.CLASS_POOL);
    }


    @Override
    protected AttributeInfo getParameterAnnotationsAttribute() {
        // 参数1：@Suspended AsyncResponse
        // 参数2: @CookieParam String tokenId
        return getParameterAnnotationsAttributeWith(
                new Annotation(Suspended.class.getName(), getContext().getConstPool()),
                getContext().tokenParam()
        );
//        ParameterAnnotationsAttribute attributeInfo = new ParameterAnnotationsAttribute(
//                getContext().getConstPool(), ParameterAnnotationsAttribute.visibleTag);
//
//        Param[] params = getUnit().getParameters();
//        Annotation[][] annotations = new Annotation[params.length + 1][];
//
//        for (int i = 0; i < params.length; i++) {
//            String pathParamValue = getPathParam(params[i]);
//            if (pathParamValue != null) {
//                annotations[i + 1] = new Annotation[]{getContext().pathParam(pathParamValue)};
//            } else {
//                annotations[i + 1] = new Annotation[0];
//            }
//        }
//        annotations[0] = new Annotation[]{new Annotation(Suspended.class.getName(), getContext().getConstPool())};
//
//        attributeInfo.setAnnotations(annotations);
//        return attributeInfo;
    }

    @Override
    protected String[] getContentType() {
//        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
//        String charsetStr = ConcreteHelper.getProfile().getString("jsr339.charset", "utf8");
//        if (!Common.isBlank(charsetStr)) {
//            try {
//                mediaType = mediaType.withCharset(Charset.forName(charsetStr).displayName());
//            } catch (UnsupportedCharsetException e) {
//                log.warn("unsupported charset: {}", charsetStr);
//            }
//        }
        return new String[]{JSR339Common.withCharset(MediaType.APPLICATION_JSON_TYPE).toString()};
//        return null;
    }

}
