package de.chappe.telegraph.simulation.line;

import java.util.concurrent.atomic.AtomicReference;

public final class SignalDisplayState {

    private final AtomicReference<String> currentSignal = new AtomicReference<>("00");

    public String currentSignal() {
        return currentSignal.get();
    }

    public void updateSignal(String signal) {
        currentSignal.set(signal);
    }
}
