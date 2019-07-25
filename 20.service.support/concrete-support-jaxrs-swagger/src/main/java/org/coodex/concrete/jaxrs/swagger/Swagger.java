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

package org.coodex.concrete.jaxrs.swagger;

import org.coodex.concrete.jaxrs.DefaultJaxrsClassGetter;
import org.coodex.concrete.jaxrs.Polling;
import org.coodex.concrete.jaxrs.ServiceRegisteredListener;
import org.coodex.util.Common;
import org.coodex.util.Singleton;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@Path("swagger")
public class Swagger implements DefaultJaxrsClassGetter, ServiceRegisteredListener {

    private final static Logger log = LoggerFactory.getLogger(Swagger.class);

    private static Singleton<Set<Class>> classes = new Singleton<>(
            LinkedHashSet::new
    );

    private static SingletonMap<String, String> swaggerJson = new SingletonMap<>(
            (key) -> SwaggerHelper.toJson(key, new ArrayList<>(classes.get()))
    );

    private static String[] swagger_files = {
            "favicon-16x16.png",
            "favicon-32x32.png",
            "index.html",
            "oauth2-redirect.html",
            "swagger-ui.css",
            "swagger-ui.js",
            "swagger-ui-bundle.js",
            "swagger-ui-standalone-preset.js"
    };

    private static SingletonMap<String, StaticFileContent> staticFileContents =
            new SingletonMap<>(key -> {
                if (Common.inArray(key, swagger_files)) {
                    try {
                        String type = "text/html";
                        if (key.endsWith(".css")) {
                            type = "text/css";
                        } else if (key.endsWith(".js")) {
                            type = "application/javascript";
                        } else if (key.endsWith(".png")) {
                            type = "image/png";
                        }
                        InputStream inputStream = Common.getResource("swagger/" + key).openStream();
                        try {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            Common.copyStream(inputStream, byteArrayOutputStream);
                            StaticFileContent fileContent = new StaticFileContent();
                            fileContent.type = type;
                            fileContent.content = byteArrayOutputStream.toByteArray();
                            return fileContent;
                        } finally {
                            inputStream.close();
                        }
                    } catch (Throwable e) {
                        log.warn("load file failed: {}.", key, e);
                    }
                }
                return null;
            });

    @Context
    private UriInfo uri;

    @Override
    public Class[] getClasses() {
        return new Class[]{Swagger.class};
    }

    @Override
    public void register(Object instance, Class concreteService) {
        if (Polling.class.equals(concreteService)) return;
        // TODO 如何区分不同的应用
        classes.get().add(concreteService);
    }

    @Path("config/swagger.json")
    @GET
    public Response swaggerJson() {
        return Response
                .ok()
                .type(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"))
                .entity(swaggerJson.get(getContextPath())).build();
    }

    private String getContextPath() {
        return uri.getBaseUri().getPath();
    }
//    private static SingletonMap<String, R>

    @Path("{file}")
    @GET
    public Response getStaticFile(@PathParam("file") String file) {
        StaticFileContent fileContent = staticFileContents.get(file);
        if (fileContent == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response
                .ok()
                .type(fileContent.type)
                .entity(new ByteArrayInputStream(fileContent.content))
                .build();
    }

    @GET
    public Response index() {
        return getStaticFile("index.html");
    }

    static class StaticFileContent {
        String type;
        byte[] content;
    }

}
