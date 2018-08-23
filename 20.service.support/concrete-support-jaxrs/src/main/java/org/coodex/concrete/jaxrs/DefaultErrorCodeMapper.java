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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.ErrorCodes;

import javax.ws.rs.core.Response;

public class DefaultErrorCodeMapper implements ErrorCodeMapper {
    @Override
    public Response.Status toStatus(int errorCode) {
        switch (errorCode) {
            case ErrorCodes.UNKNOWN_ERROR:
            case ErrorCodes.MODULE_DEFINITION_NOT_FOUND:
            case ErrorCodes.UNIT_DEFINITION_NOT_FOUND:
            case ErrorCodes.NO_BEAN_PROVIDER_FOUND:
            case ErrorCodes.NO_SERVICE_INSTANCE_FOUND:
            case ErrorCodes.BEAN_CONFLICT:
                return Response.Status.INTERNAL_SERVER_ERROR;
            case ErrorCodes.NONE_TOKEN:
            case ErrorCodes.TOKEN_INVALIDATE:
                return Response.Status.UNAUTHORIZED;
            case ErrorCodes.UNTRUSTED_ACCOUNT:
            case ErrorCodes.NO_AUTHORIZATION:
                return Response.Status.FORBIDDEN;
            case ErrorCodes.OVERRUN:
                return Response.Status.SERVICE_UNAVAILABLE;
            default:
                return Response.Status.BAD_REQUEST;
        }
    }
}
