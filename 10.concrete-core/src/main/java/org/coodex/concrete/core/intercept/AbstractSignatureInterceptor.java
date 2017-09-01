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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.codec.binary.Base64;
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.api.pojo.Signature;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.signature.SignUtil;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.*;
import static org.coodex.concrete.core.signature.SignUtil.PROFILE;

/**
 * Created by davidoff shen on 2017-04-24.
 */
public abstract class AbstractSignatureInterceptor extends AbstractSyncInterceptor {


    protected final String KEY_FIELD_ALGORITHM = "algorithm";
    protected final String KEY_FIELD_SIGN = "sign";
    protected final String KEY_FIELD_KEY_ID = "keyId";
    protected final String KEY_FIELD_NOISE = "noise";

    @Override
    public int getOrder() {
        return InterceptOrders.SIGNATURE;
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return context.getAnnotation(Signable.class) != null;
    }

    @Override
    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
        SignUtil.HowToSign howToSign = SignUtil.howToSign(context);

        int run = SIDE.get() == null ? SIDE_SERVER : SIDE.get().intValue();

        switch (run) {
            case SIDE_SERVER:
                return serverModel(context, joinPoint, howToSign);
            case SIDE_CLIENT:
                // client: 签名、验签
                return clientModel(context, joinPoint, howToSign);
            default:// 本地和测试模式不管
                return joinPoint.proceed();
        }
    }

    protected abstract Map<String, Object> buildContent(RuntimeContext context, Object[] args);

    private String getPropertyName(String propertyName) {
        return PROFILE.getString("property." + propertyName, propertyName);
    }

    private Object serverModel(RuntimeContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
        try {
            // 0 验签
            Map<String, Object> content = buildContent(context, joinPoint.getArguments());
            Assert.isNull(getKeyField(content, KEY_FIELD_NOISE, null),
                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
                    "noise MUST NOT null.");

            // 必须保留，存在向content中put数据的可能
            String algorithm = getKeyField(content, KEY_FIELD_ALGORITHM, howToSign.getAlgorithm());
            String keyId = getKeyField(content, KEY_FIELD_KEY_ID, null);
            IronPen ironPen = howToSign.getIronPenFactory().getIronPen(howToSign.getPaperName());
            SignatureSerializer serializer = howToSign.getSerializer();
            Assert.not(ironPen.verify(serializer.serialize(content),
                    Base64.decodeBase64(getSignature(content)),
                    algorithm, keyId),
                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED, "server side verify failed.");

            Object o = joinPoint.proceed();

            //1 签名
            if (o != null && o instanceof Signature) {
                return serverSign((Signature) o, algorithm, keyId, ironPen, serializer);
            } else {
                return o;
            }
        } catch (ConcreteException ce) {
            throw ce;
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }

    /**
     * @param signature
     * @param algorithm  客户端传递的算法
     * @param keyId      客户端keyId
     * @param ironPen
     * @param serializer
     * @return
     * @throws IllegalAccessException
     */
    private Object serverSign(Signature signature, String algorithm, String keyId, IronPen ironPen, SignatureSerializer serializer) throws IllegalAccessException {
        signature.setNoise(Common.random(Integer.MAX_VALUE ));
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
                SUBJOIN.get().get(propertyName);
        if(key != null){
            content.put(propertyName, key);
        }
        return key == null ? defaultValue : key.toString();
    }


    private String getSignature(Map<String, Object> content) {
        String propertySign = getPropertyName(KEY_FIELD_SIGN);
        String signStr = (String) content.remove(propertySign);
        if (signStr == null) {
            signStr = Assert.isNull(SUBJOIN.get().get(propertySign),
                    ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
                    "no signature found");
        }
        return signStr;
    }

    ////////////
    private String putKeyField(Map<String, Object> content, String keyName, Object value, RuntimeContext context, MethodInvocation joinPoint) {
        String propertyName = getPropertyName(keyName);
        if (content.containsKey(propertyName)) {
            //参数中包含
            if (content != null)
                setArgument(context, joinPoint, propertyName, value);
        } else {
            if (value != null)
                SUBJOIN.get().set(propertyName, Arrays.asList(value.toString()));
        }
        content.put(propertyName, value);
        return value == null ? null : value.toString();
    }

    private Object clientModel(RuntimeContext context, MethodInvocation joinPoint, SignUtil.HowToSign howToSign) {
        try {
            // 0 签名
            Map<String, Object> content = buildContent(context, joinPoint.getArguments());
            // noise
            putKeyField(content, KEY_FIELD_NOISE, Common.random(0, Integer.MAX_VALUE), context, joinPoint);

            //algorithm
            String algorithm = putKeyField(content, KEY_FIELD_ALGORITHM,
                    SignUtil.getString(KEY_FIELD_ALGORITHM, howToSign.getPaperName(), null),
                    context, joinPoint);
            if(algorithm == null)
                algorithm = howToSign.getAlgorithm();

            // keyId
            String keyId = putKeyField(content, KEY_FIELD_KEY_ID,
                    SignUtil.getString(KEY_FIELD_KEY_ID, howToSign.getPaperName(), null),
                    context, joinPoint);

            String sign = Base64.encodeBase64String(howToSign.getIronPenFactory().getIronPen(howToSign.getPaperName())
                    .sign(howToSign.getSerializer().serialize(content), algorithm, keyId));

            putKeyField(content, KEY_FIELD_SIGN, sign, context, joinPoint);
            Object o = joinPoint.proceed();

            if (o != null && o instanceof Signature) {
                clientVerify(howToSign, (Signature) o, algorithm, keyId);
            }
            return o;
        } catch (ConcreteException ce) {
            throw ce;
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }

    private void clientVerify(SignUtil.HowToSign howToSign, Signature signature, String algorithm, String keyId) throws IllegalAccessException {
        Assert.not(howToSign.getIronPenFactory().getIronPen(howToSign.getPaperName())
                .verify(howToSign.getSerializer().serialize(signatureToMap(signature)),
                        Base64.decodeBase64(signature.getSign()),
                        algorithm, keyId), ErrorCodes.SIGNATURE_VERIFICATION_FAILED, "client side verify failed.");
    }


    protected abstract void setArgument(RuntimeContext context, MethodInvocation joinPoint, String parameterName, Object value);
}
