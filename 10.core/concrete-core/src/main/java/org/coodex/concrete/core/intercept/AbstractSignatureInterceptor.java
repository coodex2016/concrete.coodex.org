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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.codec.binary.Base64;
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.api.pojo.Signature;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.concrete.core.intercept.annotations.ClientSide;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.core.signature.SignUtil;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.core.signature.SignUtil.*;

/**
 * Created by davidoff shen on 2017-04-24.
 */
@ServerSide
@ClientSide
public abstract class AbstractSignatureInterceptor extends AbstractInterceptor {


    private final static Logger log = LoggerFactory.getLogger(AbstractSignatureInterceptor.class);


    private final String KEY_FIELD_ALGORITHM = "algorithm";
    private final String KEY_FIELD_SIGN = "sign";
    private final String KEY_FIELD_KEY_ID = "keyId";
    private final String KEY_FIELD_NOISE = "noise";

    @Override
    public int getOrder() {
        return InterceptOrders.SIGNATURE;
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
//        ServiceContext serviceContext = getServiceContext();
        return context.getAnnotation(Signable.class) != null;
//        &&
//                (serviceContext instanceof ServerSideContext ||
//                        serviceContext instanceof ClientSideContext);
    }

//    private int getModel() {
//        return getServiceContext().getSide() == null ? SIDE_SERVER : getServiceContext().getSide().intValue();
//    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        SignUtil.HowToSign howToSign = SignUtil.howToSign(context);
        ServiceContext serviceContext = getServiceContext();
        if (serviceContext instanceof ServerSideContext) {
            serverSide_Verify(context, joinPoint, howToSign);
        } else if (serviceContext instanceof ClientSideContext) {
            clientSide_Sign(context, joinPoint, howToSign);
        }
    }

    /**
     * TODO 通过subjoin来传递
     */
    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        SignUtil.HowToSign howToSign = SignUtil.howToSign(context);
        ServiceContext serviceContext = getServiceContext();
        if (serviceContext instanceof ServerSideContext) {
            return serverSide_Sign(context, joinPoint, howToSign, result);
        } else if (serviceContext instanceof ClientSideContext) {
            return clientSide_Verify(context, joinPoint, howToSign, result);
        }
        return result;
    }


    //    @Override
//    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
//        SignUtil.HowToSign howToSign = SignUtil.howToSign(context);
//
//        int run = SIDE.get() == null ? SIDE_SERVER : SIDE.get().intValue();
//
//        switch (run) {
//            case SIDE_SERVER:
//                return serverModel(context, joinPoint, howToSign);
//            case SIDE_CLIENT:
//                // client: 签名、验签
//                return clientModel(context, joinPoint, howToSign);
//            default:// 本地和测试模式不管
//                return joinPoint.proceed();
//        }
//    }

//    protected abstract AbstractUnit getUnitFromContext(RuntimeContext context/*, Object[] args*/);

//    protected AbstractUnit

//    protected abstract Map<String, Object> buildContent(RuntimeContext context, Object[] args);

    private Map<String, Object> buildContent(DefinitionContext context, Object[] args) {
        AbstractUnit unit = AModule.getUnit(context.getDeclaringClass(), context.getDeclaringMethod());
        AbstractParam[] params = unit.getParameters();
        if (params == null) return new HashMap<>();
        // 1个参数的情况
        if (params.length == 1) {
            Class c = params[0].getType();
            // 非集合、数组、基础类型
            if (!Collection.class.isAssignableFrom(c) && !c.isArray() && !TypeHelper.isPrimitive(c)) {
                try {
                    return beanToMap(args[0]);
                } catch (Throwable th) {
                    throw ConcreteHelper.getException(th);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();

        for (AbstractParam param : unit.getParameters()) {
            result.put(param.getName(), args[param.getIndex()]);
        }
        return result;
    }

    private String getPropertyName(String propertyName) {
//        return PROFILE.getString("property." + propertyName, propertyName);
        return Config.getValue("property." + propertyName, propertyName, TAG_SIGNATRUE, getAppSet());
    }

    private void serverSide_Verify(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
        Map<String, Object> content = buildContent(
                context, joinPoint.getArguments());
        String noise = IF.isNull(getKeyField(content, KEY_FIELD_NOISE, null),
                ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
                KEY_FIELD_NOISE + " MUST NOT null.");

        // 必须保留，存在向content中put数据的可能
        String algorithm = getKeyField(content, KEY_FIELD_ALGORITHM, howToSign.getAlgorithm());
        String keyId = getKeyField(content, KEY_FIELD_KEY_ID, null);
        // 检验noise有效性
        getNoiseValidator(keyId).checkNoise(keyId,noise);
        IronPen ironPen = howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName());
        SignatureSerializer serializer = howToSign.getSerializer();
        IF.not(ironPen.verify(serializer.serialize(content),
                Base64.decodeBase64(getSignature(content)),
                algorithm, keyId),
                ErrorCodes.SIGNATURE_VERIFICATION_FAILED, "server side verify failed.");

    }

    private Object serverSide_Sign(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign, Object o) {
        try {
            if (o instanceof Signature) {
                Map<String, Object> content = buildContent(
                        context, joinPoint.getArguments());

                // 必须保留，存在向content中put数据的可能
                String algorithm = getKeyField(content, KEY_FIELD_ALGORITHM, howToSign.getAlgorithm());
                String keyId = getKeyField(content, KEY_FIELD_KEY_ID, null);
                IronPen ironPen = howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName());
                SignatureSerializer serializer = howToSign.getSerializer();
                return serverSign((Signature) o, algorithm, keyId, ironPen, serializer);
            } else {
                return o;
            }
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

//    @Deprecated
//    private Object serverModel(RuntimeContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
//        try {
//            // 0 验签
//            Map<String, Object> content = buildContent(CURRENT_UNIT.get(), joinPoint.getArguments());
//            IF.isNull(getKeyField(content, KEY_FIELD_NOISE, null),
//                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
//                    KEY_FIELD_NOISE + " MUST NOT null.");
//
//            // 必须保留，存在向content中put数据的可能
//            String algorithm = getKeyField(content, KEY_FIELD_ALGORITHM, howToSign.getAlgorithm());
//            String keyId = getKeyField(content, KEY_FIELD_KEY_ID, null);
//            IronPen ironPen = howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName());
//            SignatureSerializer serializer = howToSign.getSerializer();
//            IF.not(ironPen.verify(serializer.serialize(content),
//                    Base64.decodeBase64(getSignature(content)),
//                    algorithm, keyId),
//                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED, "server side verify failed.");
//
//            Object o = joinPoint.proceed();
//
//            //1 签名
//            if (o != null && o instanceof Signature) {
//                return serverSign((Signature) o, algorithm, keyId, ironPen, serializer);
//            } else {
//                return o;
//            }
//        } catch (Throwable th) {
//            throw ConcreteHelper.getException(th);
//        }
//    }

    /**
     * @param signature 签名对象
     * @param algorithm  客户端传递的算法
     * @param keyId      客户端keyId
     * @param ironPen   ironPen
     * @param serializer    serializer
     * @return
     * @throws IllegalAccessException
     */
    private Object serverSign(Signature signature, String algorithm, String keyId, IronPen ironPen, SignatureSerializer serializer) throws IllegalAccessException {
//        signature.setNoise(Common.random(Integer.MAX_VALUE));
        signature.setNoise(getNoiseGenerator(keyId).generateNoise());
        signature.setSign(
                Base64.encodeBase64String(
                        ironPen.sign(serializer.serialize(signatureToMap(signature)), algorithm, keyId)));
        return signature;
    }


    private Map<String, Object> signatureToMap(Signature signature) throws IllegalAccessException {
        if (signature == null) return null;
        Map<String, Object> result = new HashMap<String, Object>();
        for (Field field : ReflectHelper.getAllDeclaredFields(signature.getClass())) {
            if (!field.getDeclaringClass().equals(Signature.class) || KEY_FIELD_NOISE.equals(field.getName())) {
                field.setAccessible(true);
                result.put(field.getName(), field.get(signature));
            }
        }
        return result;
    }

    private String getKeyField(Map<String, Object> content, String keyName, String defaultValue) {
        String propertyName = getPropertyName(keyName);
        Object key = content.containsKey(propertyName) ?
                content.get(propertyName) :
                getServiceContext().getSubjoin().get(propertyName);
        if (key != null) {
            content.put(propertyName, key);
        }
        return key == null ? defaultValue : key.toString();
    }


    private String getSignature(Map<String, Object> content) {
        String propertySign = getPropertyName(KEY_FIELD_SIGN);
        String signStr = (String) content.remove(propertySign);
        if (signStr == null) {
            signStr = IF.isNull(getServiceContext().getSubjoin().get(propertySign),
                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
                    "no signature found");
        }
        return signStr;
    }

    ////////////
    private String putKeyField(Map<String, Object> content, String keyName, Object value, DefinitionContext context, MethodInvocation joinPoint) {
        String propertyName = getPropertyName(keyName);
        if (content.containsKey(propertyName)) {
            //参数中包含
            if (content != null)
                setArgument(context, joinPoint, propertyName, value);
        } else {
            if (value != null)
                getServiceContext().getSubjoin().set(propertyName, Arrays.asList(value.toString()));
        }
        content.put(propertyName, value);
        return value == null ? null : value.toString();
    }

    private void clientSide_Sign(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
        // 0 签名
        Map<String, Object> content = buildContent(
                context, joinPoint.getArguments());

        // keyId
        String keyId = putKeyField(content, KEY_FIELD_KEY_ID,
                SignUtil.getString(KEY_FIELD_KEY_ID, howToSign.getPaperName(), null),
                context, joinPoint);

        // noise
//        int noise = Common.random(0, Integer.MAX_VALUE);
        String noise = getNoiseGenerator(keyId).generateNoise();

        putKeyField(content, KEY_FIELD_NOISE, noise, context, joinPoint);

        //algorithm
        String algorithm = putKeyField(content, KEY_FIELD_ALGORITHM,
                SignUtil.getString(KEY_FIELD_ALGORITHM, howToSign.getPaperName(), null),
                context, joinPoint);
        if (algorithm == null)
            algorithm = howToSign.getAlgorithm();



        byte[] data = howToSign.getSerializer().serialize(content);

        String sign = Base64.encodeBase64String(howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName())
                .sign(data, algorithm, keyId));

        putKeyField(content, KEY_FIELD_SIGN, sign, context, joinPoint);
        if(log.isDebugEnabled()) {
            log.debug("signature for[ {} ]: \n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}",
                    context.getDeclaringMethod().getName(),
                    getPropertyName(KEY_FIELD_NOISE), noise,
                    getPropertyName(KEY_FIELD_ALGORITHM), algorithm,
                    getPropertyName(KEY_FIELD_KEY_ID), keyId,
                    getPropertyName(KEY_FIELD_SIGN), sign,
                    "body", dataToString(data)
//                    "data",
            );
        }
    }

    private Object clientSide_Verify(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign, Object o) {
        try {
            if (o != null && o instanceof Signature) {
                // 0 签名
                Map<String, Object> content = buildContent(
                        context, joinPoint.getArguments());

                //algorithm
                String algorithm = putKeyField(content, KEY_FIELD_ALGORITHM,
                        SignUtil.getString(KEY_FIELD_ALGORITHM, howToSign.getPaperName(), null),
                        context, joinPoint);
                if (algorithm == null)
                    algorithm = howToSign.getAlgorithm();

                // keyId
                String keyId = putKeyField(content, KEY_FIELD_KEY_ID,
                        SignUtil.getString(KEY_FIELD_KEY_ID, howToSign.getPaperName(), null),
                        context, joinPoint);

                clientVerify(howToSign, (Signature) o, algorithm, keyId);
            }
            return o;
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

//    @Deprecated
//    private Object clientModel(RuntimeContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
//        try {
//            // 0 签名
//            Map<String, Object> content = buildContent(CURRENT_UNIT.get(), joinPoint.getArguments());
//            // noise
//            int noise = Common.random(0, Integer.MAX_VALUE);
//            putKeyField(content, KEY_FIELD_NOISE, noise, context, joinPoint);
//
//            //algorithm
//            String algorithm = putKeyField(content, KEY_FIELD_ALGORITHM,
//                    SignUtil.getString(KEY_FIELD_ALGORITHM, howToSign.getPaperName(), null),
//                    context, joinPoint);
//            if (algorithm == null)
//                algorithm = howToSign.getAlgorithm();
//
//            // keyId
//            String keyId = putKeyField(content, KEY_FIELD_KEY_ID,
//                    SignUtil.getString(KEY_FIELD_KEY_ID, howToSign.getPaperName(), null),
//                    context, joinPoint);
//
//            byte[] data = howToSign.getSerializer().serialize(content);
//
//            String sign = Base64.encodeBase64String(howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName())
//                    .sign(data, algorithm, keyId));
//
//            putKeyField(content, KEY_FIELD_SIGN, sign, context, joinPoint);
//            log.debug("signature for[ {} ]: \n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}",
//                    context.getActualMethod().getName(),
//                    getPropertyName(KEY_FIELD_NOISE), noise,
//                    getPropertyName(KEY_FIELD_ALGORITHM), algorithm,
//                    getPropertyName(KEY_FIELD_KEY_ID), keyId,
//                    getPropertyName(KEY_FIELD_SIGN), sign,
//                    "toSign", dataToString(data)
////                    "data",
//            );
//            Object o = joinPoint.proceed();
//
//            if (o != null && o instanceof Signature) {
//                clientVerify(howToSign, (Signature) o, algorithm, keyId);
//            }
//            return o;
//        } catch (ConcreteException ce) {
//            throw ce;
//        } catch (Throwable th) {
//            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
//        }
//    }

    private void clientVerify(SignUtil.HowToSign howToSign, @NotNull Signature signature, String algorithm, String keyId) throws IllegalAccessException {
        IF.not(howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName())
                .verify(howToSign.getSerializer().serialize(signatureToMap(signature)),
                        Base64.decodeBase64(signature.getSign()),
                        algorithm, keyId), ErrorCodes.SIGNATURE_VERIFICATION_FAILED, "client side verify failed.");
    }


//    protected abstract void setArgument(RuntimeContext context, MethodInvocation joinPoint, String parameterName, Object value);


    protected void setArgument(DefinitionContext context, MethodInvocation joinPoint, String parameterName, Object value) {
        AbstractUnit unit = AModule.getUnit(context.getDeclaringClass(), context.getDeclaringMethod());//getServiceContext().getCurrentUnit();
        for (AbstractParam param : unit.getParameters()) {
            if (param.getName().equals(parameterName)) {
                joinPoint.getArguments()[param.getIndex()] = value;
                break;
            }
        }
    }

    protected abstract String dataToString(byte[] data);

//    protected abstract String serialize(Map<String, Object>)

    protected String methodToProperty(Method method) {
        if (method.getParameterTypes().length != 0) return null;

        if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
            return null;
        }
        String methodName = method.getName();
        if (methodName.startsWith("get")) {
            return Common.lowerFirstChar(methodName.substring(3));
        } else if (methodName.startsWith("is") &&
                (method.getReturnType().equals(boolean.class) || method.getReturnType().equals(Boolean.class))) {
            return Common.lowerFirstChar(methodName.substring(2));
        }
        return null;
    }

    protected Map<String, Object> beanToMap(Object bean) throws InvocationTargetException, IllegalAccessException {
        Class c = bean.getClass();
        Map<String, Object> objectMap = new HashMap<String, Object>();
        for (Method method : c.getMethods()) {
            String property = methodToProperty(method);
            if (property != null) {
                method.setAccessible(true);
                Object o = method.invoke(bean);
                if (o != null) objectMap.put(property, o);
            }

        }
        return objectMap;
    }
}
