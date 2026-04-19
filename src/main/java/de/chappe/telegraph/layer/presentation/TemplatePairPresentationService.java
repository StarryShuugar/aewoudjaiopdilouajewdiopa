package de.chappe.telegraph.layer.presentation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.chappe.telegraph.layer.application.ApplicationSentence;
import de.chappe.telegraph.layer.application.SentenceTemplate;

public final class TemplatePairPresentationService implements PresentationServiceAccessPoint {

    private final Map<SentenceTemplate, SignalPair> pairByTemplate;
    private final Map<SignalPair, SentenceTemplate> templateByPair;

    public TemplatePairPresentationService() {
        Map<SentenceTemplate, SignalPair> mutablePairByTemplate = new EnumMap<>(SentenceTemplate.class);
        mutablePairByTemplate.put(SentenceTemplate.ENDPOINT_A_ANNOUNCES, new SignalPair(10, 11));
        mutablePairByTemplate.put(SentenceTemplate.TRANSMISSION_READY, new SignalPair(12, 13));

        this.pairByTemplate = Map.copyOf(mutablePairByTemplate);
        this.templateByPair = new HashMap<>();
        for (Map.Entry<SentenceTemplate, SignalPair> entry : pairByTemplate.entrySet()) {
            templateByPair.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public List<SignalPair> encode(ApplicationSentence sentence) {
        List<SignalPair> signalPairs = new ArrayList<>();
        for (SentenceTemplate template : sentence.templates()) {
            SignalPair signalPair = pairByTemplate.get(template);
            if (signalPair == null) {
                throw new IllegalArgumentException("No signal pair registered for template " + template);
            }
            signalPairs.add(signalPair);
        }
        return List.copyOf(signalPairs);
    }

    @Override
    public ApplicationSentence decode(List<SignalPair> signalPairs) {
        List<SentenceTemplate> templates = new ArrayList<>();
        for (SignalPair signalPair : signalPairs) {
            SentenceTemplate template = templateByPair.get(signalPair);
            if (template == null) {
                throw new IllegalArgumentException("No sentence template registered for pair " + signalPair);
            }
            templates.add(template);
        }
        return new ApplicationSentence(templates);
    }
}
