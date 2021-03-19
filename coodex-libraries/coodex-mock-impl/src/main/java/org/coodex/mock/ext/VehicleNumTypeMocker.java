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
import org.coodex.util.Common;

import java.lang.reflect.Type;

import static org.coodex.util.Common.randomChar;

public class VehicleNumTypeMocker extends AbstractTypeMocker<VehicleNum> {

    @Override
    protected Class<?>[] getSupportedClasses() {
        return new Class<?>[]{String.class};
    }

    @Override
    protected boolean accept(VehicleNum annotation) {
        return annotation != null;
    }

    @Override
    public Object mock(VehicleNum mockAnnotation, Type targetType) {
        if (mockAnnotation.belong().length == 0) return null;

        VehicleNum.Province province = mockAnnotation.belong()[Common.random(mockAnnotation.belong().length - 1)];
        StringBuilder builder = new StringBuilder();
        builder.append(province.getX());
        String codeRange = new String(province.getCode());
        if (mockAnnotation.governmentVehicle())
            codeRange += 'O';
        builder.append(randomChar(codeRange));


        int alphabetCount = 0;
        String alphabets = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String numbers = "0123456789";
        for (int i = 1; i < 5; i++) {
            if (alphabetCount < 2 && Math.random() < 0.1) {
                builder.append(randomChar(alphabets));
                alphabetCount++;
            } else {
                builder.append(randomChar(numbers));
            }
        }

        String lastRange = numbers + alphabets;
        if (mockAnnotation.coachVehicle())
            lastRange += '学';
        if (mockAnnotation.towedVehicle())
            lastRange += '挂';
        builder.append(randomChar(lastRange));

        return builder.toString();
    }
}
