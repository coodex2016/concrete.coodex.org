package org.coodex.concrete.fsm;

public class SignaledState implements State {
    private long signal;

    public final long getSignal() {
        return signal;
    }

    public final void setSignal(long signal) {
        this.signal = signal;
    }
}
