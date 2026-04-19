package de.chappe.telegraph.layer.presentation;

import java.util.List;

import de.chappe.telegraph.layer.application.ApplicationSentence;

public interface PresentationServiceAccessPoint {

    List<SignalPair> encode(ApplicationSentence sentence);

    ApplicationSentence decode(List<SignalPair> signalPairs);
}
