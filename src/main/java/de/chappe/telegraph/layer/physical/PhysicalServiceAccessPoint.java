package de.chappe.telegraph.layer.physical;

import java.util.List;

import de.chappe.telegraph.layer.presentation.SignalPair;

public interface PhysicalServiceAccessPoint {

    SignalSequence encode(List<SignalPair> signalPairs);

    List<SignalPair> decode(SignalSequence signalSequence);
}
