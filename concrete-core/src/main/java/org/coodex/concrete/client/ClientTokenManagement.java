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

package org.coodex.concrete.client;

import org.coodex.util.Common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO 支持持久化
 */
public class ClientTokenManagement {
    private static Map<String, String> tokens = new ConcurrentHashMap<String, String>();

    public static void setTokenId(Destination destination, String tokenId) {
        if (!Common.isBlank(tokenId) && !destination.isTokenTransfer()) {
            synchronized (tokens) {
                tokens.put(
                        Common.isBlank(destination.getTokenManagerKey()) ?
                                destination.getLocation() :
                                destination.getTokenManagerKey(),
                        tokenId);
            }
        }
    }

    public static String getTokenId(Destination destination) {
        return tokens.get(
                Common.isBlank(destination.getTokenManagerKey()) ?
                        destination.getLocation() :
                        destination.getTokenManagerKey());
    }

    public static String getTokenId(Destination destination, String currentTokenId) {
        return destination.isTokenTransfer() ? currentTokenId : getTokenId(destination);
    }
}
