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

package org.coodex.concrete.core.signature;

import org.coodex.concrete.api.Signable;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.concrete.core.intercept.AbstractSignatureInterceptor;
import org.coodex.config.Config;
import org.coodex.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

//import static org.coodex.concrete.common.ConcreteHelper.getProfile;

/**
 * Created by davidoff shen on 2017-04-21.
 */
public class SignUtil {

    public static final String KEY_FIELD_ALGORITHM = "algorithm";
    public static final String KEY_FIELD_SIGN = "sign";
    public static final String KEY_FIELD_KEY_ID = "keyId";
    public static final String KEY_FIELD_NOISE = "noise";
    public static final String TAG_SIGNATRUE = "signature";
    //    public static final Profile PROFILE = getProfile(TAG_SIGNATRUE);
    private static final LazySelectableServiceLoader<String, IronPenFactory> IRON_PEN_FACTORY_CONCRETE_SPI_FACADE
            = new LazySelectableServiceLoader<String, IronPenFactory>() {
    };
    private static final SignatureSerializer DEFAULT_SERIALIZER = new DefaultSignatureSerializer();
    private static final ServiceLoader<SignatureSerializer> SIGNATURE_SERIALIZER_CONCRETE_SPI_FACADE
            = new ServiceLoaderImpl<SignatureSerializer>(DEFAULT_SERIALIZER) {
    };
    private static final NoiseValidator defaultValidator = new NoiseValidator() {
        @Override
        public void checkNoise(String keyId, String noise) {

        }

        @Override
        public boolean accept(String param) {
            return true;
        }
    };
    private static final LazySelectableServiceLoader<String, NoiseValidator> validatorLoader =
            new LazySelectableServiceLoader<String, NoiseValidator>(defaultValidator) {
            };
    private static final NoiseGenerator defaultGenerator = new NoiseGenerator() {
        @Override
        public String generateNoise() {
            return String.valueOf(Common.random(0, Integer.MAX_VALUE));
        }

        @Override
        public boolean accept(String param) {
            return true;
        }
    };
    private static final LazySelectableServiceLoader<String, NoiseGenerator> generatorLoader =
            new LazySelectableServiceLoader<String, NoiseGenerator>(defaultGenerator) {
            };

    public static String getKeyId() {
        return SubjoinWrapper.getInstance().get(AbstractSignatureInterceptor.getPropertyName(KEY_FIELD_KEY_ID));
    }

    public static String getAlgorithm() {
        return SubjoinWrapper.getInstance().get(AbstractSignatureInterceptor.getPropertyName(KEY_FIELD_ALGORITHM));
    }

    public static String getSign() {
        return SubjoinWrapper.getInstance().get(AbstractSignatureInterceptor.getPropertyName(KEY_FIELD_SIGN));
    }

    public static String getNoise() {
        return SubjoinWrapper.getInstance().get(AbstractSignatureInterceptor.getPropertyName(KEY_FIELD_NOISE));
    }

    public static String methodToProperty(Method method) {
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

    public static Map<String, Object> beanToMap(Object bean) throws InvocationTargetException, IllegalAccessException {
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

    public static Map<String, Object> buildContent(DefinitionContext context, Object[] args) {
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

    private static String getString(Profile profile, String key, String paperName) {
        return Common.isBlank(paperName) ? profile.getString(key) : profile.getString(key + "." + paperName);
//        if (Common.isBlank(paperName))
//            return profile.getString(key);
//        String s = profile.getString(key + "." + paperName);
//        return s == null ? getString(profile, key, null) : s;
    }

    private static String getStr(String key, String paperName, String... namespaces) {
        return Common.isBlank(paperName) ?
                Config.get(key, namespaces) :
                Config.get(key + "." + paperName, namespaces);
    }

    public static NoiseValidator getNoiseValidator(String keyId) {
        NoiseValidator noiseValidator = validatorLoader.select(keyId);
        return noiseValidator == null ? defaultValidator : noiseValidator;
    }

    public static NoiseGenerator getNoiseGenerator(String keyId) {
        NoiseGenerator noiseGenerator = generatorLoader.select(keyId);
        return noiseGenerator == null ? defaultGenerator : noiseGenerator;
    }

    public static String getString(String key, String paperName, String defaultValue) {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        String value;
        String module = null;
        if (serviceContext instanceof ClientSideContext) {
            module = ((ClientSideContext) serviceContext).getDestination().getIdentify();
        }
        value = getStr(key, paperName, TAG_SIGNATRUE, module, getAppSet());
        return value == null ? defaultValue : value;
    }

    public static HowToSign howToSign(Signable signable) {

        String algorithm = getString("algorithm", signable.paperName(), signable.algorithm());
        return new HowToSign(
                IRON_PEN_FACTORY_CONCRETE_SPI_FACADE.select(algorithm),
                signable.serializer().equals(SignatureSerializer.class) ?
                        DEFAULT_SERIALIZER :
                        SIGNATURE_SERIALIZER_CONCRETE_SPI_FACADE.get(signable.serializer()),
                signable.paperName(),
                algorithm
        );
    }

    public static HowToSign howToSign(DefinitionContext context) {
        return howToSign(context.getAnnotation(Signable.class));
    }

    public static class HowToSign {
        private final IronPenFactory ironPenFactory;
        private final SignatureSerializer serializer;
        private final String paperName;
        private final String algorithm;

        HowToSign(IronPenFactory ironPenFactory, SignatureSerializer serializer, String paperName, String algorithm) {
            this.ironPenFactory = ironPenFactory;
            this.serializer = serializer;
            this.paperName = Common.isBlank(paperName) ? null : paperName;
            this.algorithm = algorithm;
        }

        public String getPaperName() {
            return paperName;
        }

        @Deprecated
        public IronPenFactory getIronPenFactory() {
            return getIronPenFactory(null);
        }

        public IronPenFactory getIronPenFactory(String algorithm) {
            return algorithm == null ? ironPenFactory :
                    IRON_PEN_FACTORY_CONCRETE_SPI_FACADE.select(algorithm);
        }

        public SignatureSerializer getSerializer() {
            return serializer;
        }

        public String getAlgorithm() {
            return algorithm;
        }
    }
}
