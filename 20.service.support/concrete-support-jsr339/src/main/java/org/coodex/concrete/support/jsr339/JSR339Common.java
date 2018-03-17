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

package org.coodex.concrete.support.jsr339;

import org.coodex.concrete.common.ConcreteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class JSR339Common {

    private final static Logger log = LoggerFactory.getLogger(JSR339Common.class);

    public static MediaType withCharset(MediaType type) {
        try {
            return type.withCharset(
                    Charset.forName(
                            ConcreteHelper.getProfile().getString("jsr339.charset", "utf8")
                    ).displayName()
            );
        } catch (UnsupportedCharsetException e) {
            log.warn("UnsupportedCharset: {}", e.getCharsetName());
            return type;
        }
    }
}
