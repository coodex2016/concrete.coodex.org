/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.support.jaxrs.javassist;

import javassist.CannotCompileException;
import javassist.CtConstructor;
import javassist.bytecode.SignatureAttribute;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.concrete.jaxrs.ClassGenerator;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Unit;


/**
 * Created by davidoff shen on 2016-11-24.
 */
public abstract class AbstractJavassistClassGenerator implements ClassGenerator {

    protected final static String BYTE_CODE_TOOLS_NAME = "javassist";

    private int ref = 0;

    protected String nextPostfix() {
        return String.format("%04x", ++ref);
    }


    /**
     * @param module
     * @return
     * @throws CannotCompileException
     */
    public Class<?> generatesImplClass(Module module) throws CannotCompileException {

        CGContext context = initImplClass(module);
        for (Unit unit : module.getUnits()) {
            AbstractMethodGenerator methodGenerator = getMethodGenerator(context, unit);
            if (methodGenerator != null)
                context.getNewClass().addMethod(methodGenerator.generateMethod(
                        unit.getMethod().getName() + "$" + nextPostfix()));
        }
        return context.getNewClass().toClass();
    }

    private CGContext initImplClass(Module module) throws CannotCompileException {

        Class<?> serviceClass = module.getInterfaceClass();

        CGContext context = new CGContext(serviceClass, getSuperClass(),
                serviceClass.getName() + getImplPostfix());

        // 定义类泛型参数
        context.getNewClass().setGenericSignature(new SignatureAttribute.ClassSignature(null,
                JavassistHelper.classType(getSuperClass().getName(), serviceClass.getName()),
                null).encode());

        // 设置java文件信息
        context.getClassFile().setVersionToJava5();

        // @Path(value = serviceName)
        context.getClassFile().addAttribute(
                JavassistHelper.aggregate(context.getConstPool(),
                        context.path(module.getName()),
                        context.createInfo(null)));

        // 构造方法
        CtConstructor spiConstructor = new CtConstructor(null, context.getNewClass());
        spiConstructor.setBody("{super();}");
        context.getNewClass().addConstructor(spiConstructor);
        return context;
    }

    protected abstract AbstractMethodGenerator getMethodGenerator(CGContext context, Unit unit);


}
