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

import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Singleton;

public class BillCalculator {


    private static final
    Singleton<AcceptableServiceLoader<Chargeable, Calculator<Chargeable>>> CALCULATOR_LOADER =
            new Singleton<AcceptableServiceLoader<Chargeable, Calculator<Chargeable>>>(new Singleton.Builder<AcceptableServiceLoader<Chargeable, Calculator<Chargeable>>>() {
                @Override
                public AcceptableServiceLoader<Chargeable, Calculator<Chargeable>> build() {
                    return new AcceptableServiceLoader<Chargeable, Calculator<Chargeable>>() {
                    };
                }
            });


    public static <C extends Chargeable> Bill<C> calc(C chargeable) {
        if (chargeable == null) throw new NullPointerException("chargeable is null.");
        Calculator<Chargeable> calculator = CALCULATOR_LOADER.get().select(chargeable);
        if (calculator == null) {
            throw new RuntimeException("no Calculator instance found for " + chargeable.getClass() + ". " + chargeable.toString());
        }
        //noinspection unchecked
        return (Bill<C>) calculator.calc(chargeable);
    }
}
