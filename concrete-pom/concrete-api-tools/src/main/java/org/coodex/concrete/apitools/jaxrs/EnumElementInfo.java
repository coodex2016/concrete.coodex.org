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

package org.coodex.concrete.apitools.jaxrs;

import org.coodex.concrete.api.Description;
import org.coodex.util.Common;
import org.coodex.util.Described;
import org.coodex.util.JSONSerializer;
import org.coodex.util.Valuable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumElementInfo {
    private final String label;
    private final String desc;
    private final String key;
    private final String value;
    private final boolean str;

    public EnumElementInfo(Enum<?> enumElement) {
        this.key = enumElement.name();
        Optional<Description> description = getAnnotation(enumElement, Description.class);
        this.label = getLabelFromEnum(enumElement, description);
        this.desc = description.map(Description::description).orElse("--");
        if (enumElement instanceof Valuable) {
            Valuable<?> v = (Valuable<?>) enumElement;
            Object value = v.getValue();
            this.str = value instanceof String;
            this.value = str ? (String) value : JSONSerializer.getInstance().toJson(value);
        } else {
            this.str = true;
            this.value = enumElement.name();
        }
    }

    private static <A extends Annotation> Optional<A> getAnnotation(
            Enum<?> enumElement,
            @SuppressWarnings("SameParameterValue") Class<A> annotationClass) {
        try {
            return Optional.ofNullable(enumElement.getClass().getField(enumElement.name()).getAnnotation(annotationClass));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    private static String getLabelFromEnum(
            Enum<?> enumElement,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Description> annotationOptional) {
        return Common.firstValuable(
                annotationOptional.map(Description::name).orElse(null),
                Described.getDesc(enumElement),
                enumElement.name()
        );
    }

    public static List<EnumElementInfo> of(Class<Enum<?>> enumClass) {
        return Stream.of(enumClass.getEnumConstants())
                .map(EnumElementInfo::new)
                .collect(Collectors.toList());
    }

    public String getCodeValue() {
        return str ? JSONSerializer.getInstance().toJson(value) : value;
    }

    public String getLabel() {
        return label;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isStr() {
        return str;
    }

    public String getDesc() {
        return desc;
    }
}
