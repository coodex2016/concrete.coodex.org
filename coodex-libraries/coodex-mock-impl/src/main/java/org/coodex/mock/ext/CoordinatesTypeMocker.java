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

package org.coodex.mock.ext;

import org.coodex.mock.AbstractTypeMocker;
import org.coodex.mock.NumberTypeMocker;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import static org.coodex.util.GenericTypeHelper.typeToClass;

public class CoordinatesTypeMocker extends AbstractTypeMocker<Coordinates> {

    @Override
    protected Class<?>[] getSupportedClasses() {
        return new Class<?>[]{
                float[].class, Float[].class,
                double[].class, Double[].class,
                float.class, Float.class,
                Double.class, double.class,
                Coordinates.Value.class
        };
    }

    @Override
    protected boolean accept(Coordinates annotation) {
        return annotation != null;
    }

    @Override
    public Object mock(Coordinates mockAnnotation, Type targetType) {
        Class<?> c = typeToClass(targetType);
        if (c.isArray()) {
            Object result = Array.newInstance(c.getComponentType(), 2);
            Array.set(result, 0, NumberTypeMocker.mock(c.getComponentType(), mockAnnotation.longitude(), mockAnnotation.digits()));
            Array.set(result, 1, NumberTypeMocker.mock(c.getComponentType(), mockAnnotation.latitude(), mockAnnotation.digits()));
            return result;
        }

        if (Coordinates.Value.class.equals(targetType)) {
            Coordinates.Value value = new Coordinates.Value();
            value.setLongitude((Double) NumberTypeMocker.mock(Double.class, mockAnnotation.longitude(), mockAnnotation.digits()));
            value.setLatitude((Double) NumberTypeMocker.mock(Double.class, mockAnnotation.latitude(), mockAnnotation.digits()));
            return value;
        }

        return NumberTypeMocker.mock(targetType,
                mockAnnotation.dimension().equals(Coordinates.Dimension.LONGITUDE) ? mockAnnotation.longitude() : mockAnnotation.latitude(),
                mockAnnotation.digits());

    }
}
