package de.chappe.telegraph.layer.application;

public enum SentenceTemplate {
    ENDPOINT_A_ANNOUNCES("ENDPOINT_A meldet sich."),
    TRANSMISSION_READY("Übertragung bereit.");

    private final String text;

    SentenceTemplate(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
