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

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ErrorInfo;
import org.coodex.concrete.common.ThrowableMapperFacade;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.InvocationTargetException;

import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;

/**
 * Created by davidoff shen on 2016-11-01.
 */
@Provider
public class ConcreteExceptionMapper implements ExceptionMapper<Throwable> {


    private final static Logger log = LoggerFactory.getLogger(ConcreteExceptionMapper.class);
    private final static ServiceLoader<ErrorCodeMapper> CODE_MAPPER_SERVICE_LOADER = new ServiceLoaderImpl<ErrorCodeMapper>(new DefaultErrorCodeMapper()) {
    };


    /**
     * 根据错误号返回响应的响应状态，4xx为客户端问题，5xx为服务端问题，也可由项目自行重载
     *
     * @param errorCode
     * @return
     */
    protected Response.Status getStatus(int errorCode) {
        return CODE_MAPPER_SERVICE_LOADER.get().toStatus(errorCode);
    }


    @Override
    public Response toResponse(Throwable exception) {
//        int errorCode = ErrorCodes.UNKNOWN_ERROR;
//
//        ConcreteException concreteException = ConcreteHelper.findException(exception);
//
//
//        if (concreteException != null) {
//            errorCode = concreteException.getCode();
//            exception = concreteException;
//        }
        ErrorInfo errorInfo = ThrowableMapperFacade.toErrorInfo(exception);
        Response.Status status = getStatus(errorInfo.getCode());

        if (status.getFamily() == Response.Status.Family.SERVER_ERROR) {
            Throwable th = exception;
            if (exception instanceof ConcreteException) {
                if (((ConcreteException) exception).getCode() != ErrorCodes.UNKNOWN_ERROR) {
                    th = null;
                } else {
                    th = exception.getCause();
                    if (th.getClass().getName().startsWith(InvocationTargetException.class.getPackage().getName())) {
                        th = th.getCause();
                    }
                }
            }
            if (th != null) {
                log.warn("exception occurred: {}", th.getLocalizedMessage(), th);
            } else {
                log.warn("exception occurred: {}", exception.getLocalizedMessage());
            }
        }


        return Response.status(status)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header(HEADER_ERROR_OCCURRED, true)
                .entity(errorInfo)
                .build();
    }
}
