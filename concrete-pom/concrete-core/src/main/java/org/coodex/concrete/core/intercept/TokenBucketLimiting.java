/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.api.LimitingStrategy;
import org.coodex.concrete.api.limiting.TokenBucket;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.config.Config;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class TokenBucketLimiting implements LimitingStrategy {
    private final static Logger log = LoggerFactory.getLogger(TokenBucketLimiting.class);

    private static String DEFAULT_BUCKET = Common.getUUIDStr();

    private static final SingletonMap<String, Bucket> BUCKET_SINGLETON_MAP
            = SingletonMap.<String, Bucket>builder()
            .function(key -> {
                String[] namespace;
                if (DEFAULT_BUCKET.equalsIgnoreCase(key)) {
                    namespace = new String[]{"tokenBucket", getAppSet()};
                } else {
                    namespace = new String[]{"tokenBucket", getAppSet(), key};
                }
                int capacity = Math.max(1,
                        Config.getValue("capacity",
                                Common.toInt(System.getProperty("tokenBucket.capacity"), Integer.MAX_VALUE >> 1),
                                namespace));
                int flow = Math.max(1,
                        Config.getValue("flow",
                                Common.toInt(System.getProperty("tokenBucket.flow"), Short.MAX_VALUE),
                                namespace));
                Bucket bucket = new Bucket(capacity, flow);
                bucket.name = key;
                return bucket;
            }).build();

    @Override
    public boolean apply(DefinitionContext definitionContext) {
        String bucketName = DEFAULT_BUCKET;
        int used = 1;

        TokenBucket tokenBucket = definitionContext.getAnnotation(TokenBucket.class);
        if (tokenBucket != null) {
            if (!Common.isBlank(tokenBucket.bucket()))
                bucketName = tokenBucket.bucket();
            used = Math.max(used, tokenBucket.tokenUsed());
        }
        return BUCKET_SINGLETON_MAP.get(bucketName).alloc(used);
    }

    @Override
    public void release(DefinitionContext definitionContext) {
        // do nothing
    }

    @Override
    public boolean accept(DefinitionContext param) {
        return param.getAnnotation(TokenBucket.class) != null;
    }

    private static class Bucket {
        private final int capacity;
        private final int flow;
        private long lastChecked;
        private int count;
        private String name;

        private Bucket(int capacity, int flow) {
            this.capacity = capacity;
            this.flow = flow;
            this.lastChecked = Clock.currentTimeMillis();
            this.count = capacity;
        }

        private void flowIn() {
            long x = Clock.currentTimeMillis();
            if (x == lastChecked) return;
            int flowIn = (int) ((1.0d * flow) * (x - lastChecked) / 1000);
            if (flowIn > 0) {
                int beforeFlowIn = count;
                count = Math.min(capacity, beforeFlowIn + flowIn);
                int actuallyFlowIn = count - beforeFlowIn;
                if (actuallyFlowIn > 0) {
                    log.info("[{}]tokens flow in: {}, current: {}, from {} to {}",
                            DEFAULT_BUCKET.equals(name) ? "GLOBAL" : name,
                            actuallyFlowIn, count, lastChecked, x);
                }
                lastChecked = x;
            }
        }


        synchronized boolean alloc(int tokenCount) {
            flowIn();
            if (count >= tokenCount) {
                count -= tokenCount;
                log.info("[{}]tokens picked: {}, remain: {}",
                        DEFAULT_BUCKET.equals(name) ? "GLOBAL" : name,
                        tokenCount, count);
                return true;
            } else {
                return false;
            }
        }
    }

}
