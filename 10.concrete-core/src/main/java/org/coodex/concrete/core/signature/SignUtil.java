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

package org.coodex.concrete.core.signature;

import org.coodex.concrete.api.Signable;
import org.coodex.concrete.common.ConcreteSPIFacade;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.IronPenFactory;
import org.coodex.concrete.common.SignatureSerializer;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.util.AcceptableServiceSPIFacade;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.coodex.util.SPIFacade;

/**
 * Created by davidoff shen on 2017-04-21.
 */
public class SignUtil {


    public static final Profile PROFILE = Profile.getProfile("signature.properties");


    public static String getString(String key, String paperName, String defaultValue) {
        if (Common.isBlank(paperName))
            return PROFILE.getString(key, defaultValue);
        String s = PROFILE.getString(key + "." + paperName, defaultValue);
        return s == null ? getString(key, null, defaultValue) : s;
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

        public IronPenFactory getIronPenFactory() {
            return ironPenFactory;
        }

        public SignatureSerializer getSerializer() {
            return serializer;
        }

        public String getAlgorithm() {
            return algorithm;
        }
    }

    private static final AcceptableServiceSPIFacade<String, IronPenFactory> IRON_PEN_FACTORY_CONCRETE_SPI_FACADE
            = new AcceptableServiceSPIFacade<String, IronPenFactory>() {
    };

    private static final SignatureSerializer DEFAULT_SERIALIZER = new DefaultSignatureSerializer();
    private static final SPIFacade<SignatureSerializer> SIGNATURE_SERIALIZER_CONCRETE_SPI_FACADE
            = new ConcreteSPIFacade<SignatureSerializer>() {
        @Override
        protected SignatureSerializer getDefaultProvider() {
            return DEFAULT_SERIALIZER;
        }
    };

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
        return howToSign(context.getDeclaringAnnotation(Signable.class));
    }

    public static HowToSign howToSign(AbstractUnit unit) {
        return howToSign((Signable) unit.getAnnotation(Signable.class));
    }
}
