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
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.ResourceScanner;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Path("")
public class Swagger implements DefaultJaxrsClassGetter, ServiceRegisteredListener {

    private final static Logger log = LoggerFactory.getLogger(Swagger.class);

    private static final Singleton<Set<Class<?>>> classes = Singleton.with(
            LinkedHashSet::new
    );

    private static final SingletonMap<String, String> swaggerJson = SingletonMap.<String, String>builder()
            .function((key) -> SwaggerHelper.toJson(key, new ArrayList<>(classes.get()))).build();


    private static final Singleton<String[]> swaggerFiles = Singleton.with(() -> {
        List<String> files = new ArrayList<>();
        final String path = "swagger";
        ResourceScanner.newBuilder((url, str) -> {
            files.add(str.substring(path.length() + 1));
        }).build().scan(path);
        return files.toArray(new String[0]);
    });


    private static final SingletonMap<String, StaticFileContent> staticFileContents = SingletonMap.<String, StaticFileContent>builder()
            .function(key -> {
                if (Common.inArray(key, swaggerFiles.get())) {
                    try {
                        String type = "text/html";
                        if (key.endsWith(".css")) {
                            type = "text/css";
                        } else if (key.endsWith(".js")) {
                            type = "application/javascript";
                        } else if (key.endsWith(".png")) {
                            type = "image/png";
                        }
                        try (InputStream inputStream = Common.getResource("swagger/" + key).openStream()) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            Common.copyStream(inputStream, byteArrayOutputStream);
                            StaticFileContent fileContent = new StaticFileContent();
                            fileContent.type = type;
                            fileContent.content = byteArrayOutputStream.toByteArray();
                            return fileContent;
                        }
                    } catch (Throwable e) {
                        log.warn("load file failed: {}.", key, e);
                    }
                }
                return null;
            }).build();

    @Context
    private UriInfo uri;
    @Context
    private UriInfo uriInfo;

    @Override
    public Class<?>[] getClasses() {
        return Config.getValue("swagger.enabled", true) ?
                new Class<?>[]{Swagger.class} :
                new Class<?>[0];
    }

    @Override
    public void register(Object instance, Class<?> concreteService) {
        if (Polling.class.equals(concreteService)) {
            return;
        }
        // TODO 如何区分不同的应用
        classes.get().add(concreteService);
    }

    private String getContextPath() {
        return uri.getBaseUri().getPath();
    }
//    private static SingletonMap<String, R>

    @Path("swagger/config/swagger.json")
    @GET
    public Response swaggerJson() {
        return Response
                .ok()
                .type(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"))
                .entity(swaggerJson.get(getContextPath())).build();
    }

    @Path("swagger/{file}")
    @GET
    public Response getStaticFile(@PathParam("file") String file) {
        if (Common.isBlank(file)) {
            file = "index.html";
        }
        StaticFileContent fileContent = staticFileContents.get(file);
        if (fileContent == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response
                .ok()
                .type(fileContent.type)
                .entity(new ByteArrayInputStream(fileContent.content))
                .build();
    }

    @GET
    @Path("swagger")
    public Response index() throws URISyntaxException {
        return Response.seeOther(new URI(uriInfo.getBaseUri() + "swagger/index.html")).build();
    }

    static class StaticFileContent {
        String type;
        byte[] content;
    }


}
