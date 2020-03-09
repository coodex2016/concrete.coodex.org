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

package org.coodex.util.clock;

import org.coodex.config.Config;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

import static org.coodex.util.Common.getSystemStart;

public class DefaultClockAgent extends AbstractClockAgent {
    private final static Logger log = LoggerFactory.getLogger(DefaultClockAgent.class);

    public static final String KEY_BASELINE = Clock.class.getName() + ".baseline";

    public DefaultClockAgent() {
        super(Clock.getMagnification(), getTheBaseLine(), getSystemStart());
    }

    private static long getTheBaseLine() {
        Long l = toBaseLine(Config.get(KEY_BASELINE, "clock"));
        if (l == null) {
            l = toBaseLine(System.getProperty(KEY_BASELINE));
        }
        return l == null ? getSystemStart() : l.longValue();
    }

    private static Long toBaseLine(String str) {
        if (!Common.isBlank(str) && !str.equalsIgnoreCase("now")) {
            try {
                return Common.strToDate(str).getTime();
            } catch (ParseException e) {
                log.warn("baseline parse error: {}", str, e);
            }
        }
        return null;
    }
}
