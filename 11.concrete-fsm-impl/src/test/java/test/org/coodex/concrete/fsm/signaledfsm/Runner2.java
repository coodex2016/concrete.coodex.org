package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.fsm.FiniteStateMachineProvider;
import org.coodex.concrete.fsm.SignaledState;
import org.coodex.concrete.fsm.WrongStateException;

public class Runner2 {

    public static void main(String[] args) {
        final DemoSignaledState state = new DemoSignaledState();
        state.setValue(0);
        final FiniteStateMachineProvider provider = new ConcreteServiceLoader<FiniteStateMachineProvider>() {
        }.getInstance();

        for (int i = 0; i < 300; i++) {
            final int x = i % 4;
            new Thread() {
                @Override
                public void run() {
                    FSMDemo2 fsmDemo = provider.getMachine(state, FSMDemo2.class);
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
                    } catch (WrongStateException e) {
                        System.out.println(String.format("Wrong state [%d] to %d[thread: %d]",
                                ((SignaledState)e.getState()).getSignal(), x, Thread.currentThread().getId()));
                    }
                }
            }.start();
        }
    }
}
