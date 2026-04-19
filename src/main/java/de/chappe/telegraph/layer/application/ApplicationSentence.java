package de.chappe.telegraph.layer.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record ApplicationSentence(List<SentenceTemplate> templates) {

    public ApplicationSentence {
        Objects.requireNonNull(templates, "templates must not be null");
        if (templates.isEmpty()) {
            throw new IllegalArgumentException("templates must not be empty");
        }
    }

    public String text() {
        return templates.stream()
                .map(SentenceTemplate::text)
                .collect(Collectors.joining(" "));
    }
}
