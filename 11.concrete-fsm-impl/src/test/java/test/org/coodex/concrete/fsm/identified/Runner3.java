package test.org.coodex.concrete.fsm.identified;

import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.fsm.FiniteStateMachineProvider;
import org.coodex.concrete.fsm.IdentifiedStateContainer;
import org.coodex.concrete.fsm.IdentifiedStateIsLockingException;
import org.coodex.util.Common;
import test.org.coodex.concrete.fsm.DemoWrongStateException;

public class Runner3 {

    private static final FiniteStateMachineProvider provider = new ConcreteServiceLoader<FiniteStateMachineProvider>() {
    }.getInstance();

    private static final IdentifiedStateContainer container = new ConcreteServiceLoader<IdentifiedStateContainer>() {
    }.getInstance();


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
