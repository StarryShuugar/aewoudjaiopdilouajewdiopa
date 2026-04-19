package de.chappe.telegraph.simulation.line;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public final class UdpSignalClient {

    private static final int BUFFER_SIZE = 1024;
    private static final int OBSERVATION_TIMEOUT_MILLIS = 200;

    public String observeSignal(String host, int displayPort) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(0));
            channel.configureBlocking(false);
            channel.send(ByteBuffer.wrap(UdpStationProtocol.OBSERVE_SIGNAL.getBytes(StandardCharsets.UTF_8)),
                    new InetSocketAddress(host, displayPort));

            long deadline = System.currentTimeMillis() + OBSERVATION_TIMEOUT_MILLIS;
            ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (System.currentTimeMillis() < deadline) {
                responseBuffer.clear();
                if (channel.receive(responseBuffer) == null) {
                    pauseObservation();
                    continue;
                }

                responseBuffer.flip();
                byte[] responseBytes = new byte[responseBuffer.remaining()];
                responseBuffer.get(responseBytes);
                String response = new String(responseBytes, StandardCharsets.UTF_8);
                return UdpStationProtocol.extractObservedSignal(response);
            }

            return "00";
        }
    }

    public void displaySignal(String host, int operatorPort, String signal) throws IOException {
        String response = sendRequest(host, operatorPort, UdpStationProtocol.displaySignalRequest(signal));
        if (!UdpStationProtocol.OK.equals(response)) {
            throw new IOException("Unexpected operator response: " + response);
        }
    }

    private String sendRequest(String host, int port, String request) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(0));
            channel.configureBlocking(true);
            channel.send(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), new InetSocketAddress(host, port));

            ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            channel.receive(responseBuffer);
            responseBuffer.flip();
            byte[] responseBytes = new byte[responseBuffer.remaining()];
            responseBuffer.get(responseBytes);
            return new String(responseBytes, StandardCharsets.UTF_8);
        }
    }

    private static void pauseObservation() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
