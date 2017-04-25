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

package org.coodex.concrete.jaxrs.client;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class ClientException extends ConcreteException {
    private final int code;
    private final String path;
    private final String method;

    private final String msg;

    public ClientException(int code, String msg, String path, String method) {
        super(ErrorCodes.CLIENT_ERROR);
        this.code = code;
        this.path = path;
        this.method = method;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
