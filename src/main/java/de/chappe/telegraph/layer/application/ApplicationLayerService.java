package de.chappe.telegraph.layer.application;

import java.util.List;

public final class ApplicationLayerService {

    public ApplicationSentence createHappyPathSentence() {
        return new ApplicationSentence(List.of(
                SentenceTemplate.ENDPOINT_A_ANNOUNCES,
                SentenceTemplate.TRANSMISSION_READY));
    }
}
