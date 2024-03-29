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

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.common.ErrorInfo;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.config.Config;
import org.coodex.mock.Mocker;
import org.coodex.util.JSONSerializer;
import org.coodex.util.PojoInfo;
import org.coodex.util.PojoProperty;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static org.coodex.util.GenericTypeHelper.solveFromType;
import static org.coodex.util.GenericTypeHelper.toReference;

@SuppressWarnings("unused")
public class SwaggerHelper {

    private static final ThreadLocal<Map<String, Schema<?>>> definitions = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> readyForSchema = new ThreadLocal<>();

    public static OpenAPI toOpenAPI(String url, List<Class<?>> classes) {
        readyForSchema.remove();
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(Config.getValue("swagger.title",
                                "concrete-swagger",
                                "concrete", "swagger"))
                        .description(Config.getValue("swagger.description",
                                "Thanks for choosing concrete",
                                "concrete", "swagger"))
                        .version(Config.getValue("swagger.version", "1.0.0", "concrete", "swagger"))
                        .contact(new Contact()
                                .name(Config.getValue("swagger.contacts.name", "concrete", "concrete", "swagger"))
                                .url(Config.getValue("swagger.contacts.url", "https://concrete.coodex.org", "concrete"
                                        , "swagger"))
                        )
                )
                .addServersItem(new Server()
                        .url(url))
                .paths(new Paths());
        Set<Tag> tags = new LinkedHashSet<>();
        definitions.set(new LinkedHashMap<>());
        try {
            for (Class<?> clz : classes) {
                JaxrsModule module = new JaxrsModule(clz);
                tags.add(new Tag().name(module.getLabel()).description(module.getDescription()));
                for (JaxrsUnit unit : module.getUnits()) {
                    String name = unit.getDeclaringModule().getName() + unit.getName();
                    openAPI.path(name,
                            toPathItem(unit, openAPI.getPaths().get(name)));
                }
            }
            openAPI.tags(new ArrayList<>(tags));
            Components components = new Components();
            Map<String, Schema<?>> defined = definitions.get();
            for (Map.Entry<String, Schema<?>> entry : defined.entrySet()) {
                components.addSchemas(entry.getKey(), entry.getValue());
            }

//            components.addSchemas("ErrorInfo", classSchema(ErrorInfo.class));
            openAPI.components(components);

        } finally {
            definitions.remove();
        }
        return openAPI;
    }


    private static PathItem toPathItem(JaxrsUnit unit, PathItem item) {
        if (item == null) {
            item = new PathItem();
        }
        Operation operation = new Operation();
        operation.setSummary(unit.getLabel());
        operation.setDescription(unit.getDescription());
        operation.addTagsItem(unit.getDeclaringModule().getLabel());

        Parameter parameter = new Parameter()
                .name("concrete-token-id")
                .in("header")
                .required(unit.getAccessAllow() != null)
                .allowEmptyValue(false)
                .description("Concrete Token")
                .schema(schema(String.class));
        operation.addParametersItem(parameter);


        for (JaxrsParam param : unit.getParameters()) {
            if (param.isPathParam()) {
                parameter = new Parameter();
                parameter.name(param.getName())
                        .in(param.isPathParam() ? "path" : "body")
                        .description(param.getDescription())
                        .required(true)
                        .allowEmptyValue(false)
                        .schema(schema(toReference(param.getGenericType(),
                                unit.getDeclaringModule().getInterfaceClass())))
                        .addExample("default",
                                new Example().value(Mocker.mockParameter(unit.getMethod(), param.getIndex(),
                                        unit.getDeclaringModule().getInterfaceClass()))
                        );
                operation.addParametersItem(parameter);
            }
        }

        if (!"get".equalsIgnoreCase(unit.getInvokeType()) || unit.getPojoCount() != 0) {
            RequestBody body = new RequestBody();
            Content content = new Content();
            MediaType mediaType = new MediaType();
            if (!unit.isAssembledPojo() && unit.getPojoCount() > 0) {
                JaxrsParam param = unit.getPojo()[0];
                mediaType.schema(
                                schema(toReference(param.getGenericType(),
                                        unit.getDeclaringModule().getInterfaceClass()))
                                        .title(param.getLabel()).description(param.getDescription())
                        )
                        .addExamples("default", new Example().value(
                                Mocker.mockParameter(unit.getMethod(), param.getIndex(),
                                        unit.getDeclaringModule().getInterfaceClass())
                        ));
            } else {
                Map<String, Object> mocked = new HashMap<>();
                Schema<?> objectSchema = new Schema<>();

                for (JaxrsParam param : unit.getPojo()) {
                    objectSchema.addProperties(param.getName(),
                            schema(toReference(param.getGenericType(), unit.getDeclaringModule().getInterfaceClass()))
                                    .title(param.getLabel()).description(param.getDescription())
                    );
                    mocked.put(param.getName(), Mocker.mockParameter(unit.getMethod(), param.getIndex(),
                            unit.getDeclaringModule().getInterfaceClass()));
                }
                objectSchema.example(mocked);
                mediaType.schema(objectSchema).addExamples("default",
                        new Example().value(mocked));
            }
            content.addMediaType("application/json", mediaType);
            body.content(content);
            operation.requestBody(body);
        }


        operation.responses(new ApiResponses().addApiResponse("200",
                        new ApiResponse().content(
                                new Content().addMediaType("application/json",
                                        new MediaType().schema(
                                                schema(toReference(unit.getGenericReturnType(),
                                                        unit.getDeclaringModule().getInterfaceClass()))
                                        ).example(
                                                Mocker.mockMethod(unit.getMethod(),
                                                        unit.getDeclaringModule().getInterfaceClass())
                                        )))).addApiResponse("204", new ApiResponse())
                .addApiResponse("default", new ApiResponse()
                        .content(new Content().addMediaType("application/json", new MediaType()
                                .schema(schema(ErrorInfo.class)))))
        );
        switch (unit.getInvokeType().toLowerCase()) {
            case "get":
                item.get(operation);
                break;
            case "post":
                item.post(operation);
                break;
            case "put":
                item.put(operation);
                break;
            case "delete":
                item.delete(operation);
                break;
        }
        return item;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    private static Schema<?> schema(Type type) {
        if (type instanceof Class) {
            return classSchema((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            Class<?> c = (Class<?>) ((ParameterizedType) type).getRawType();
            if (Collection.class.isAssignableFrom(c)) {
                Type t = solveFromType(Collection.class.getTypeParameters()[0], type);
                if (byte.class.equals(t)) {
                    return new ByteArraySchema();
                } else {
                    return new ArraySchema().items(schema(t));
                }
            } else if (Map.class.isAssignableFrom(c)) {
                // todo map怎么搞？
            } else {
                return pojoSchema(type);
            }
        } else if (type instanceof GenericArrayType) {
            return new ArraySchema().items(
                    schema(((GenericArrayType) type).getGenericComponentType())
            );
        } else {
            // ??
        }
        return new Schema<>();

    }

    private static Schema<?> classSchema(Class<?> c) {
        if (byte[].class.equals(c)) {
            return new ByteArraySchema();
        } else if (c.isArray()) {
            return new ArraySchema().items(classSchema(c.getComponentType()));
        } else if (void.class.equals(c) || Void.class.equals(c)) {
            return new ObjectSchema();
        } else if (Byte.class.equals(c) || int.class.equals(c) || Integer.class.equals(c)
                || short.class.equals(c) || Short.class.equals(c)) {
            return new IntegerSchema();
        } else if (long.class.equals(c) || Long.class.equals(c)) {
            return new IntegerSchema().format("int64");
        } else if (float.class.equals(c) || Float.class.equals(c)) {
            return new NumberSchema().format("float");
        } else if (double.class.equals(c) || Double.class.equals(c)) {
            return new NumberSchema().format("double");
        } else if (char.class.equals(c) || Character.class.equals(c) || String.class.equals(c)) {
            return new StringSchema();
        } else if (Date.class.equals(c) || Calendar.class.equals(c)) {
            return new DateTimeSchema();
        } else {
            return pojoSchema(c);
        }
    }

    private static Schema<?> pojoSchema(Type t) {
        Map<String, Schema<?>> defined = definitions.get();
        String name = t.toString().replace(' ', '_');
        if (!defined.containsKey(name)) {
            Set<String> set = readyForSchema.get();
            boolean remove = set == null;
            if (set == null) {
                set = new LinkedHashSet<>();
                readyForSchema.set(set);
            }
            try {
                if (!set.contains(name)) {
                    set.add(name);
                    Schema<?> objectSchema = new Schema<>();
                    PojoInfo pojoInfo = new PojoInfo(t);
                    for (PojoProperty property : pojoInfo.getProperties()) {
                        Schema<?> schema = schema(property.getType());
                        Description description = property.getAnnotation(Description.class);
                        if (description != null) {
                            schema.title(description.name()).description(description.description());
                        }
                        objectSchema.addProperties(property.getName(), schema);
                    }
                    defined.put(name, objectSchema);
                } else {
                    return new Schema<>().title("cycle ref").description(name);
                }
            } finally {
                if (remove) {
                    set.remove(name);
                }
            }

        }
        return new Schema<>().$ref("#/components/schemas/" + name);
    }


    public static String toJson(String url, List<Class<?>> classes) {
        return JSONSerializer.getInstance().toJson(toOpenAPI(url, classes));
    }

    public static String toYaml(String url, List<Class<?>> classes) {
        return Yaml.pretty(toOpenAPI(url, classes));
    }

}
