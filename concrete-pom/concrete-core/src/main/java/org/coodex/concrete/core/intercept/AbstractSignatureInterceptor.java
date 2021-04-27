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
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.api.pojo.Signature;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.concrete.core.intercept.annotations.ClientSide;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.core.signature.Client4Elements;
import org.coodex.concrete.core.signature.ClientKeyIdAndAlgGetter;
import org.coodex.concrete.core.signature.SignUtil;
import org.coodex.config.Config;
import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;
import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.core.signature.SignUtil.*;

/**
 * Created by davidoff shen on 2017-04-24.
 */
@ServerSide
@ClientSide
public abstract class AbstractSignatureInterceptor extends AbstractInterceptor {


    private final static Logger log = LoggerFactory.getLogger(AbstractSignatureInterceptor.class);

    private static final ServiceLoader<ClientKeyIdAndAlgGetter> CLIENT_KEY_ID_GETTER = new LazyServiceLoader<ClientKeyIdAndAlgGetter>(
            (paperName, propertyKeyId, destination) -> {
                String s = Config.get("signature." + propertyKeyId, TAG_CLIENT, destination.getIdentify(), paperName, getAppSet());
                if (s == null) {
                    s = Config.get(propertyKeyId, TAG_CLIENT, destination.getIdentify(), getAppSet());
                }
                // 兼容之前的方式
                if (s == null) {
                    s = SignUtil.getString(propertyKeyId, paperName, null);
                    if (s != null) {
                        log.warn("get client key id from namespace[{},{}] is deprecated. set in [{}, {}]",
                                TAG_SIGNATURE, destination.getIdentify(),
                                TAG_CLIENT, destination.getIdentify());
                    }
                }
                return s;
            }
    ) {
    };

    private static final ServiceLoader<Client4Elements> CLIENT_4_ELEMENTS = new LazyServiceLoader<Client4Elements>(
            (module, key) -> {
                String s = Config.get(key, TAG_CLIENT, module);
                if (s == null) {
                    s = Config.get(key, TAG_SIGNATURE, module);
                    if (s != null) {
                        log.warn("get client signature elements[{}] from [{}, {}] is deprecated. use [{}, {}] plz.",
                                key,
                                TAG_SIGNATURE, module,
                                TAG_CLIENT, module);
                    }
                }
                return s;
            }
    ) {
    };
    private static final Supplier<String> SERVER_SIDE_VERIFY_FAILED = () -> I18N.translate("sign.serverSideVerifyFailed");
    private static final Supplier<String> NO_SIGNATURE_FOUND = () -> I18N.translate("sign.noSignatureFound");

    //    private int getModel() {
//        return getServiceContext().getSide() == null ? SIDE_SERVER : getServiceContext().getSide().intValue();
//    }
    private static final SingletonMap<String, PropertyNameReload> PROPERTY_NAMES = SingletonMap.<String, PropertyNameReload>builder()
            .function(PropertyNameReload::new).nullKey("null_" + UUIDHelper.getUUIDString()).build();

    private static final Supplier<String> NOISE_MUST_NOT_NULL =
            () -> String.format(I18N.translate("sign.mustNotNull"), getPropertyName(KEY_FIELD_NOISE));

    private static void serverSide_Verify(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
        Map<String, Object> content = buildContent(
                context, joinPoint.getArguments());
        String noise = IF.isNull(getKeyField(content, KEY_FIELD_NOISE, null),
                ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
                NOISE_MUST_NOT_NULL
        );

        // 必须保留，存在向content中put数据的可能
        String algorithm = getKeyField(content, KEY_FIELD_ALGORITHM, howToSign.getAlgorithm());
        String keyId = getKeyField(content, KEY_FIELD_KEY_ID, null);

        // 检验noise有效性
        getNoiseValidator(keyId).checkNoise(keyId, noise);
        IronPen ironPen = howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName());
        SignatureSerializer serializer = howToSign.getSerializer();
        IF.not(ironPen.verify(serializer.serialize(content),
                Base64.getDecoder().decode(getSignature(content)),
                algorithm, keyId),
                ErrorCodes.SIGNATURE_VERIFICATION_FAILED, SERVER_SIDE_VERIFY_FAILED);

    }

    private static String getSignature(Map<String, Object> content) {
        String propertySign = getPropertyName(KEY_FIELD_SIGN);
        String signStr = (String) content.remove(propertySign);
        if (signStr == null) {
            signStr = IF.isNull(getServiceContext().getSubjoin().get(propertySign),
                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED, NO_SIGNATURE_FOUND);
        }
        return signStr;
    }

    private static Object serverSide_Sign(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign, Object o) {
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

    private static String getKeyField(Map<String, Object> content, String keyName, String defaultValue) {
        String propertyName = getPropertyName(keyName);
        Object key = content.containsKey(propertyName) ?
                content.get(propertyName) :
                getServiceContext().getSubjoin().get(propertyName);
        if (key != null) {
            content.put(propertyName, key);
        }
        return key == null ? defaultValue : key.toString();
    }

    private static String putKeyField(Map<String, Object> content, String keyName,
                                      Object value, DefinitionContext context, MethodInvocation joinPoint) {
        String propertyName = getPropertyName(keyName);
        if (content.containsKey(propertyName)) {
            //参数中包含
            setArgument(context, joinPoint, propertyName, value);
        } else {
            if (value != null)
                getServiceContext().getSubjoin()
                        .set(propertyName, Collections.singletonList(value.toString()));
        }
        content.put(propertyName, value);
        return value == null ? null : value.toString();
    }

    private static void setArgument(DefinitionContext context, MethodInvocation joinPoint, String parameterName, Object value) {
        AbstractUnit<?> unit = AModule.getUnit(context.getDeclaringClass(), context.getDeclaringMethod());//getServiceContext().getCurrentUnit();
        for (AbstractParam param : unit.getParameters()) {
            if (param.getName().equals(parameterName)) {
                joinPoint.getArguments()[param.getIndex()] = value;
                break;
            }
        }
    }

    public static String getPropertyName(String propertyName) {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        if (serviceContext instanceof ClientSideContext) {
            return PROPERTY_NAMES.get(((ClientSideContext) serviceContext).getDestination().getIdentify()).getName(propertyName);
        } else {
            return PROPERTY_NAMES.get(null).getName(propertyName);
        }
    }

    private static Object serverSign(Signature signature, String algorithm, String keyId, IronPen ironPen, SignatureSerializer serializer) throws IllegalAccessException {
        signature.setNoise(getNoiseGenerator(keyId).generateNoise());
        signature.setSign(
                Base64.getEncoder().encodeToString(
                        ironPen.sign(serializer.serialize(signatureToMap(signature)), algorithm, keyId)));
        return signature;
    }

    private static Map<String, Object> signatureToMap(Signature signature) throws IllegalAccessException {
        if (signature == null) return null;
        Map<String, Object> result = new HashMap<>();
        for (Field field : ReflectHelper.getAllDeclaredFields(signature.getClass())) {
            if (!field.getDeclaringClass().equals(Signature.class) || KEY_FIELD_NOISE.equals(field.getName())) {
                field.setAccessible(true);
                result.put(field.getName(), field.get(signature));
            }
        }
        return result;
    }

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


    ////////////

    private void clientSide_Sign(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {

        // 0 签名
        Map<String, Object> content = buildContent(
                context, joinPoint.getArguments());

        // keyId
        String keyId = putKeyField(content, KEY_FIELD_KEY_ID,
                CLIENT_KEY_ID_GETTER.get().getValue(
                        howToSign.getPaperName(),
                        getPropertyName(KEY_FIELD_KEY_ID),
                        ((ClientSideContext) getServiceContext()).getDestination()
                ),
                context, joinPoint);

        // noise
        String noise = getNoiseGenerator(keyId).generateNoise();

        putKeyField(content, KEY_FIELD_NOISE, noise, context, joinPoint);

        //algorithm
        String algorithm = putKeyField(content, KEY_FIELD_ALGORITHM,
                CLIENT_KEY_ID_GETTER.get().getValue(
                        howToSign.getPaperName(),
                        getPropertyName(KEY_FIELD_ALGORITHM),
                        ((ClientSideContext) getServiceContext()).getDestination()
                ),
                context, joinPoint);
        if (algorithm == null)
            algorithm = howToSign.getAlgorithm();


        byte[] data = howToSign.getSerializer().serialize(content);

        String sign = Base64.getEncoder().encodeToString(howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName())
                .sign(data, algorithm, keyId));

        putKeyField(content, KEY_FIELD_SIGN, sign, context, joinPoint);
        if (log.isDebugEnabled()) {
            log.debug("signature for[ {} ]: \n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}\n\t{}: {}",
                    context.getDeclaringMethod().getName(),
                    getPropertyName(KEY_FIELD_NOISE), noise,
                    getPropertyName(KEY_FIELD_ALGORITHM), algorithm,
                    getPropertyName(KEY_FIELD_KEY_ID), keyId,
                    getPropertyName(KEY_FIELD_SIGN), sign,
                    "body", dataToString(data)
            );
        }
    }

    private Object clientSide_Verify(DefinitionContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign, Object o) {
        try {
            if (o instanceof Signature) {
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

    private void clientVerify(SignUtil.HowToSign howToSign, @NotNull Signature signature, String algorithm, String keyId) throws IllegalAccessException {
        IF.not(howToSign.getIronPenFactory(algorithm).getIronPen(howToSign.getPaperName())
                .verify(howToSign.getSerializer().serialize(signatureToMap(signature)),
                        Base64.getDecoder().decode(signature.getSign()),
                        algorithm, keyId), ErrorCodes.SIGNATURE_VERIFICATION_FAILED, "client side verify failed.");
    }


    protected abstract String dataToString(byte[] data);


    private static class PropertyNameReload {
        private final String module;
        private final Map<String, String> mapping = new HashMap<>();

        PropertyNameReload(String module) {
            this.module = module;
            mapping.put(KEY_FIELD_KEY_ID, initLoad(KEY_FIELD_KEY_ID));
            mapping.put(KEY_FIELD_SIGN, initLoad(KEY_FIELD_SIGN));
            mapping.put(KEY_FIELD_ALGORITHM, initLoad(KEY_FIELD_ALGORITHM));
            mapping.put(KEY_FIELD_NOISE, initLoad(KEY_FIELD_NOISE));
        }

        private String initLoad(String propertyName) {
            if (Common.isBlank(module)) { // Server端
                String s = Config.get("signature.property." + propertyName, TAG_SIGNATURE, getAppSet());
                if (s == null) {
                    s = Config.getValue("property." + propertyName, propertyName, TAG_SIGNATURE, getAppSet());
                    if (!propertyName.equals(s)) {
                        log.warn("property.{} is deprecated. use signature.property.{} plz.", propertyName, propertyName);
                    }
                }
                return s == null ? propertyName : s;
            } else {
                String s = CLIENT_4_ELEMENTS.get().getElementsName(module, "signature.property." + propertyName);
                if (s == null) {
                    s = CLIENT_4_ELEMENTS.get().getElementsName(module, "property." + propertyName);
                }
                return s == null ? propertyName : s;
            }
        }

        String getName(String key) {
            return mapping.get(key);
        }
    }

}
