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

import org.coodex.mock.Mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Mock
public @interface IdCard {

    Specification specification() default Specification.S18;

    String[] divisions() default {};

    int minAge() default 5;

    int maxAge() default 90;

    Sex sex() default Sex.RANDOM;

    enum Specification {
        RANDOM(-1), S15(15), S18(18);


        private final int size;

        Specification(int size) {
            this.size = size;
        }

        public int getSize() {
            if (size == -1) {
                return Math.random() < 0.5 ? 15 : 18;
            } else
                return size;
        }
    }

    enum Sex {
        RANDOM(-1), FEMALE(0), MALE(1);

        private final int sex;

        Sex(int sex) {
            this.sex = sex;
        }

        public int getSex() {
            if (sex == -1) {
                return Math.random() < 0.5 ? 0 : 1;
            }
            return sex;
        }
    }
}
