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
package org.coodex.util;

import org.coodex.config.Config;

import java.util.Base64;
import java.util.UUID;

import static org.coodex.util.Common.base16Encode;

/**
 * @author davidoff
 */
public class UUIDHelper {
    private static final Singleton<String> CODE = Singleton.with(() -> Config.getValue("uuid.encoder", "base16"));
    private static final SelectableServiceLoader<String, Encoder> ENCODER_SERVICE_LOADER
            = new LazySelectableServiceLoader<String, Encoder>(new Base16Encoder()) {
    };

    public static byte[] getUUIDBytes() {
        UUID uuid = UUID.randomUUID();
        long longOne = uuid.getMostSignificantBits();
        long longTwo = uuid.getLeastSignificantBits();

        return new byte[]{
                (byte) (longOne >>> 56),
                (byte) (longOne >>> 48),
                (byte) (longOne >>> 40),
                (byte) (longOne >>> 32),
                (byte) (longOne >>> 24),
                (byte) (longOne >>> 16),
                (byte) (longOne >>> 8),
                (byte) longOne,
                (byte) (longTwo >>> 56),
                (byte) (longTwo >>> 48),
                (byte) (longTwo >>> 40),
                (byte) (longTwo >>> 32),
                (byte) (longTwo >>> 24),
                (byte) (longTwo >>> 16),
                (byte) (longTwo >>> 8),
                (byte) longTwo
        };
    }

    public static String getUUIDString() {
        return ENCODER_SERVICE_LOADER.select(CODE.get()).encode(getUUIDBytes());
    }

    public interface Encoder extends SelectableService<String> {
        String encode(byte[] bytes);
    }

    public static class Base16Encoder implements Encoder {

        @Override
        public String encode(byte[] bytes) {
            return base16Encode(bytes);
        }

        @Override
        public boolean accept(String param) {
            return true;
        }
    }

    public static class Base64Encoder implements Encoder {

        @Override
        public String encode(byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }

        @Override
        public boolean accept(String param) {
            return "base64".equalsIgnoreCase(param);
        }

    }

    public static class Base64UrlSafeEncoder implements Encoder {

        @Override
        public String encode(byte[] bytes) {
            return Base64.getUrlEncoder().encodeToString(bytes);
        }

        @Override
        public boolean accept(String param) {
            return "base64UrlSafe".equalsIgnoreCase(param);
        }
    }

    public static class Base58Encoder implements Encoder {

        @Override
        public String encode(byte[] bytes) {
            return Base58.encode(bytes);
        }

        @Override
        public boolean accept(String param) {
            return "base58".equalsIgnoreCase(param);
        }
    }

}
