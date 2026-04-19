package de.chappe.telegraph.layer.physical;

import java.util.ArrayList;
import java.util.List;

import de.chappe.telegraph.layer.presentation.SignalPair;

public final class PhysicalSignalService implements PhysicalServiceAccessPoint {

    @Override
    public SignalSequence encode(List<SignalPair> signalPairs) {
        List<SignalCode> signalCodes = new ArrayList<>();
        signalCodes.add(new SignalCode(99));
        for (SignalPair signalPair : signalPairs) {
            signalCodes.add(new SignalCode(signalPair.firstCode()));
            signalCodes.add(new SignalCode(signalPair.secondCode()));
        }
        signalCodes.add(new SignalCode(0));
        return new SignalSequence(signalCodes);
    }

    @Override
    public List<SignalPair> decode(SignalSequence signalSequence) {
        List<SignalCode> signalCodes = signalSequence.codes();
        if (signalCodes.size() < 3) {
            throw new IllegalArgumentException("signal sequence is too short");
        }
        if (signalCodes.getFirst().value() != 99) {
            throw new IllegalArgumentException("signal sequence must start with 99");
        }
        if (signalCodes.getLast().value() != 0) {
            throw new IllegalArgumentException("signal sequence must end with 00");
        }

        List<SignalCode> payloadCodes = signalCodes.subList(1, signalCodes.size() - 1);
        if (payloadCodes.size() % 2 != 0) {
            throw new IllegalArgumentException("payload codes must be grouped into pairs");
        }

        List<SignalPair> signalPairs = new ArrayList<>();
        for (int index = 0; index < payloadCodes.size(); index += 2) {
            signalPairs.add(new SignalPair(
                    payloadCodes.get(index).value(),
                    payloadCodes.get(index + 1).value()));
        }
        return List.copyOf(signalPairs);
    }
}
