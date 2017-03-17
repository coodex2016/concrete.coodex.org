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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public class ConcreteExceptionMapper implements ExceptionMapper<Throwable> {

    private final static Logger log = LoggerFactory.getLogger(ConcreteExceptionMapper.class);


    /**
     * 根据错误号返回响应的响应状态，4xx为客户端问题，5xx为服务端问题，也可由项目自行重载
     *
     * @param errorCode
     * @return
     */
    protected Response.Status getStatus(int errorCode) {
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
            default:
                return Response.Status.BAD_REQUEST;
        }
    }


    @Override
    public Response toResponse(Throwable exception) {
        int errorCode = ErrorCodes.UNKNOWN_ERROR;

        ConcreteException concreteException = ConcreteHelper.findException(exception);


        if (concreteException != null) {
            errorCode = concreteException.getCode();
            exception = concreteException;
        }
        Response.Status status = getStatus(errorCode);

        if (status.getFamily() == Response.Status.Family.SERVER_ERROR) {
            log.warn("exception occurred: {}", exception.getLocalizedMessage(), exception);
        }


        return Response.status(status)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header(HEADER_ERROR_OCCURRED, true)
                .entity(new ErrorInfo(errorCode, exception.getLocalizedMessage()))
                .build();
    }
}
