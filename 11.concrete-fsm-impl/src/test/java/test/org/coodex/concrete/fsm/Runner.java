package test.org.coodex.concrete.fsm;

import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.fsm.FiniteStateMachineProvider;
import org.coodex.concrete.fsm.WrongStateException;

public class Runner {


    public static void main(String[] args) {
        final NumericState state = new NumericState();
        final FiniteStateMachineProvider provider = new ConcreteServiceLoader<FiniteStateMachineProvider>() {
        }.getInstance();

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
