package de.chappe.telegraph.layer.presentation;

public record SignalPair(int firstCode, int secondCode) {

    public SignalPair {
        validateCode(firstCode);
        validateCode(secondCode);
    }

    private static void validateCode(int code) {
        if (code < 0 || code > 99) {
            throw new IllegalArgumentException("signal code must be between 0 and 99");
        }
    }
}
