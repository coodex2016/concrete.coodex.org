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

package org.coodex.billing;

import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;

import static org.coodex.util.Common.cast;

public class BillCalculator {


    private static final SelectableServiceLoader<Chargeable, Calculator<Chargeable>> CALCULATOR_LOADER =
            new LazySelectableServiceLoader<Chargeable, Calculator<Chargeable>>() {
            };

    public static <C extends Chargeable> Bill<C> calc(C chargeable) {
        if (chargeable == null) throw new NullPointerException("chargeable is null.");
        Calculator<Chargeable> calculator = CALCULATOR_LOADER.select(chargeable);
        if (calculator == null) {
            throw new RuntimeException("no Calculator instance found for " + chargeable.getClass() + ". " + chargeable.toString());
        }
        return cast(calculator.calc(chargeable));
    }

}
