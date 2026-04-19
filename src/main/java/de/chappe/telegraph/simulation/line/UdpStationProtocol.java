package de.chappe.telegraph.simulation.line;

public final class UdpStationProtocol {

    public static final String OBSERVE_SIGNAL = "OBSERVE_SIGNAL";
    public static final String SIGNAL_PREFIX = "SIGNAL:";
    public static final String DISPLAY_SIGNAL_PREFIX = "DISPLAY_SIGNAL:";
    public static final String OK = "OK";
    private static final String ERROR_PREFIX = "ERROR:";

    private UdpStationProtocol() {
    }

    public static String displaySignalRequest(String signal) {
        return DISPLAY_SIGNAL_PREFIX + signal;
    }

    public static String handleDisplayRequest(String request, String currentSignal) {
        if (!OBSERVE_SIGNAL.equals(request)) {
            return ERROR_PREFIX + "UNKNOWN_DISPLAY_REQUEST";
        }
        return SIGNAL_PREFIX + currentSignal;
    }

    public static String handleOperatorRequest(String request, SignalDisplayState signalDisplayState) {
        if (!request.startsWith(DISPLAY_SIGNAL_PREFIX)) {
            return ERROR_PREFIX + "UNKNOWN_OPERATOR_REQUEST";
        }

        String signal = request.substring(DISPLAY_SIGNAL_PREFIX.length());
        if (!signal.matches("\\d{2}")) {
            return ERROR_PREFIX + "INVALID_SIGNAL";
        }

        signalDisplayState.updateSignal(signal);
        return OK;
    }

    public static String extractObservedSignal(String response) {
        if (!response.startsWith(SIGNAL_PREFIX)) {
            throw new IllegalArgumentException("Unexpected display response: " + response);
        }
        return response.substring(SIGNAL_PREFIX.length());
    }
}
