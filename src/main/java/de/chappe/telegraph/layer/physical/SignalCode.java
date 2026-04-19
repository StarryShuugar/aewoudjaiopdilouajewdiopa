package de.chappe.telegraph.layer.physical;

public record SignalCode(int value) {

    public SignalCode {
        if (value < 0 || value > 99) {
            throw new IllegalArgumentException("signal code must be between 0 and 99");
        }
    }

    public String asTwoDigitString() {
        return "%02d".formatted(value);
    }
}
