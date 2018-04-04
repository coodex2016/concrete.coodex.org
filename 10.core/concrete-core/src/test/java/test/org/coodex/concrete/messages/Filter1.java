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

package test.org.coodex.concrete.messages;

import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.common.messages.MessageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Filter1 implements MessageFilter<Body1> {
    private final static Logger log = LoggerFactory.getLogger(Filter1.class);

    @Override
    public boolean iWantIt(Message<? extends Body1> message) {
        log.debug("filter1");
        return true;
    }
}
