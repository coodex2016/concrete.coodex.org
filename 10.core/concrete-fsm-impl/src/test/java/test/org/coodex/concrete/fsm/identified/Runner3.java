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

package test.org.coodex.concrete.fsm.identified;

import org.coodex.concrete.fsm.FiniteStateMachineProvider;
import org.coodex.concrete.fsm.IdentifiedStateContainer;
import org.coodex.concrete.fsm.IdentifiedStateIsLockingException;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoaderImpl;
import test.org.coodex.concrete.fsm.DemoWrongStateException;

public class Runner3 {

    private static final FiniteStateMachineProvider provider = new ServiceLoaderImpl<FiniteStateMachineProvider>() {
    }.get();

    private static final IdentifiedStateContainer container = new ServiceLoaderImpl<IdentifiedStateContainer>() {
    }.get();


    public static void main(String[] args) {
        final DemoIdState state = container.newStateAndLock(DemoIdState.class);
        final String id = state.getId();
//        container.release(id);


        for (int i = 0; i < 300; i++) {
            final int x = i;
            new Thread() {
                DemoIdState getStat() throws IdentifiedStateIsLockingException {
                    return x == 0 ? state : container.loadStateAndLock(id, DemoIdState.class);
                }

                @Override
                public void run() {
                    DemoIdFSM fsmDemo = null;
                    while (true) {
                        try {
                            fsmDemo =
                                    provider.getMachine(
                                            getStat(),
                                            DemoIdFSM.class);
                            break;
                        } catch (IdentifiedStateIsLockingException throwable) {
                            System.out.println(String.format(
                                    "----id: %s, msg: %s, thread: %d ",
                                    id, throwable.getLocalizedMessage(), Thread.currentThread().getId()
                            ));
                            try {
                                Thread.sleep(Common.random(300, 1000));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        switch (x % 4) {
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
                        System.out.println(String.format("Wrong state [%d] to %d[thread: %d]", e.getOldState(), x % 4, Thread.currentThread().getId()));
                    }
                }
            }.start();
        }


    }
}
