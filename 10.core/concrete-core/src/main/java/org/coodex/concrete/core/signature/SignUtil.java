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
import org.coodex.config.Config;
import org.coodex.util.*;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

//import static org.coodex.concrete.common.ConcreteHelper.getProfile;

/**
 * Created by davidoff shen on 2017-04-21.
 */
public class SignUtil {

    public static final String TAG_SIGNATRUE = "signature";

    //    public static final Profile PROFILE = getProfile(TAG_SIGNATRUE);
    private static final AcceptableServiceLoader<String, IronPenFactory> IRON_PEN_FACTORY_CONCRETE_SPI_FACADE
            = new AcceptableServiceLoader<String, IronPenFactory>(){};
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

    private static final AcceptableServiceLoader<String, NoiseValidator> validatorLoader =
            new AcceptableServiceLoader<String, NoiseValidator>(defaultValidator){};

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
    private static final AcceptableServiceLoader<String, NoiseGenerator> generatorLoader =
            new AcceptableServiceLoader<String, NoiseGenerator>(defaultGenerator){};


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
        NoiseValidator noiseValidator = validatorLoader.getServiceInstance(keyId);
        return noiseValidator == null ? defaultValidator : noiseValidator;
    }

    public static NoiseGenerator getNoiseGenerator(String keyId) {
        NoiseGenerator noiseGenerator = generatorLoader.getServiceInstance(keyId);
        return noiseGenerator == null ? defaultGenerator : noiseGenerator;
    }

    public static String getString(String key, String paperName, String defaultValue) {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        String value;
        String module = null;
        if (serviceContext instanceof ClientSideContext) {
            module = ((ClientSideContext) serviceContext).getDestination().getIdentify();
//            if (!Common.isBlank(module)) {
//                value = getString(getProfile(TAG_SIGNATRUE, module), key, paperName);
//            }
        }
//        if (value == null) {
//            value = getString(getProfile(TAG_SIGNATRUE), key, paperName);

//        }
        value = getStr(key, paperName, TAG_SIGNATRUE, module, getAppSet());
        return value == null ? defaultValue : value;
//        if (Common.isBlank(paperName))
//            return PROFILE.getString(key, defaultValue);
//        String s = PROFILE.getString(key + "." + paperName, defaultValue);
//        return s == null ? getString(key, null, defaultValue) : s;
    }

    public static HowToSign howToSign(Signable signable) {

        String algorithm = getString("algorithm", signable.paperName(), signable.algorithm());
        return new HowToSign(
                IRON_PEN_FACTORY_CONCRETE_SPI_FACADE.getServiceInstance(algorithm),
                signable.serializer().equals(SignatureSerializer.class) ?
                        DEFAULT_SERIALIZER :
                        SIGNATURE_SERIALIZER_CONCRETE_SPI_FACADE.getInstance(signable.serializer()),
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
                    IRON_PEN_FACTORY_CONCRETE_SPI_FACADE.getServiceInstance(algorithm);
        }

        public SignatureSerializer getSerializer() {
            return serializer;
        }

        public String getAlgorithm() {
            return algorithm;
        }
    }
}
