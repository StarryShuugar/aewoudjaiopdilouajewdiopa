package de.chappe.telegraph.layer.physical;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record SignalSequence(List<SignalCode> codes) {

    public SignalSequence {
        Objects.requireNonNull(codes, "codes must not be null");
        if (codes.isEmpty()) {
            throw new IllegalArgumentException("codes must not be empty");
        }
    }

    public String asWireString() {
        return codes.stream()
                .map(SignalCode::asTwoDigitString)
                .collect(Collectors.joining(" "));
    }
}
