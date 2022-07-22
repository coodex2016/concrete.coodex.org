/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.jaxrs.garble;

import org.coodex.concrete.protobuf.Concrete;
import org.coodex.concrete.protobuf.ProtobufServiceApplication;
import org.coodex.config.Config;
import org.coodex.util.Common;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@Path("")
public class GrableService {
    public static final String CONTENT_TYPE_BIN = "application/x-concrete-bin";

    @Context
    protected HttpHeaders httpHeaders;
    @Context
    protected HttpServletRequest httpRequest;

    private static byte[] xor(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((bytes[i] & 0xFF) ^ (0xFF << (i % 8)));
        }
        return bytes;
    }


    private String getRemoteAddress() {
        String xff = httpHeaders.getHeaderString("X-Forwarded-For");
        if (!Common.isBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    @Path("")
    @POST
    @Consumes(CONTENT_TYPE_BIN)
    public void invoke(byte[] bytes, @Suspended AsyncResponse asyncResponse) {
        try {
            Concrete.RequestPackage requestPackage = Concrete.RequestPackage.parseFrom(xor(bytes));
            ProtobufServiceApplication.getInstance("jaxrs").invokeService(requestPackage, getRemoteAddress(), resp -> asyncResponse.resume(Response.ok(xor(resp.toByteArray()), MediaType.APPLICATION_OCTET_STREAM).build()));
        } catch (Throwable th) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            if (Config.getValue("jaxrs.garble.trace", false, "concrete")) {
                ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
                th.printStackTrace(new PrintStream(stackTrace));
                builder = builder.type(MediaType.TEXT_PLAIN_TYPE).entity(stackTrace.toString());
            }
            asyncResponse.resume(builder.build());
        }
    }
}
