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

package org.coodex.concrete.common;

import java.lang.reflect.Field;

/**
 * Created by davidoff shen on 2016-12-01.
 */
public class ErrorDefinition implements Comparable<ErrorDefinition> {
    private final Integer errorCode;
    private final String errorMessage;
    private final String key;
    private final String fieldName;
    private final Class<?> declaringClass;

    public ErrorDefinition(Field field) {
        try {
            this.errorCode = field.getInt(null);
            this.fieldName = field.getName();
            this.declaringClass = field.getDeclaringClass();
            this.key = ErrorMessageFacade.getKey(errorCode);
            this.errorMessage = ErrorMessageFacade.getTemplate(errorCode);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    //
//    @Deprecated
//    public ErrorDefinition(int errorCode) {
//        this.errorCode = errorCode;
//        this.errorMessage = ErrorMessageFacade.getMessageTemplate(errorCode);
//
//    }

    public int getErrorCode() {
        return errorCode;
    }

    @SuppressWarnings("unused")
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int compareTo(ErrorDefinition o) {
        return errorCode.compareTo(o.errorCode);
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "ErrorDefinition{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", key='" + key + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", declaringClass=" + declaringClass +
                '}';
    }
}
