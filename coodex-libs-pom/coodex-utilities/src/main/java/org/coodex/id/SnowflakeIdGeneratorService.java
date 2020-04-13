/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.id;

import lombok.extern.slf4j.Slf4j;
import org.coodex.config.Config;
import org.coodex.util.Base58;
import org.coodex.util.Common;
import org.coodex.util.Singleton;

@Slf4j
public class SnowflakeIdGeneratorService implements IDGeneratorService {
    private final Singleton<SnowflakeIdWorker> snowflakeIdWorkerSingleton = Singleton.with(
            () -> {
                SnowflakeIdWorker snowflakeIdWorker;
                int machineId = Config.getValue("snowflake.machineId", -1);

                if (machineId != -1) {
                    snowflakeIdWorker = new SnowflakeIdWorker(machineId);
                } else {
                    int workerId = Config.getValue("snowflake.workerId", -1);
                    int dataCenterId = Config.getValue("snowflake.dataCenterId", -1);
                    if (workerId == -1 && dataCenterId == -1) {
                        log.warn("snowflake parameters[machineId, workerId, dataCenterId] not set. use default value.");
                        snowflakeIdWorker = new SnowflakeIdWorker(0);
                    } else {
                        snowflakeIdWorker = new SnowflakeIdWorker(workerId, dataCenterId);
                    }
                }
                return snowflakeIdWorker;
            }
    );

    public SnowflakeIdGeneratorService() {
    }

    @Override
    public String generateId() {
        return Base58.encode(Common.long2Bytes(snowflakeIdWorkerSingleton.get().nextId()));
    }
}
