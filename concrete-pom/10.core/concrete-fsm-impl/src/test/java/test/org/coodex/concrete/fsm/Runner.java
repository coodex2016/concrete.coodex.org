/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.FiniteStateMachineProvider;
import org.coodex.util.ServiceLoaderImpl;

public class Runner {

    private static final FiniteStateMachineProvider provider = new ServiceLoaderImpl<FiniteStateMachineProvider>() {
    }.get();


    public static void main(String[] args) {
        final NumericState state = new NumericState();

        for (int i = 0; i < 300; i++) {
            final int x = i % 4;
            new Thread() {
                @Override
                public void run() {
                    FSMDemo fsmDemo = provider.getMachine(state, FSMDemo.class);
                    try {
                        switch (x) {
                            case 0:
                                fsmDemo.toZero();
                                break;
                            case 1:
                                fsmDemo.toOne();
                                break;
                            case 2:
                                fsmDemo.toTwo();
                                break;
                            case 3:
                                fsmDemo.toThree();
                                break;
                        }
                    } catch (DemoWrongStateException e) {
                        System.out.println(String.format("Wrong state [%d] to %d[thread: %d]", e.getOldState(), x, Thread.currentThread().getId()));
                    }
                }
            }.start();
        }
    }
}
