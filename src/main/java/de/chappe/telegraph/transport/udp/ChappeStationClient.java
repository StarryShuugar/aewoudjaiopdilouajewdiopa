package de.chappe.telegraph.transport.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public final class ChappeStationClient {

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_RETURN_SIGNAL_TIMEOUT_MILLIS = 2000;

    private ChappeStationClient() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            printUsage();
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String payload = args[2];
        boolean awaitReturnSignal = args.length >= 4 && "--await-return-signal".equals(args[3]);
        int timeoutMillis = args.length >= 5 ? Integer.parseInt(args[4]) : DEFAULT_RETURN_SIGNAL_TIMEOUT_MILLIS;

        InetSocketAddress target = new InetSocketAddress(host, port);

        try (DatagramChannel channel = DatagramChannel.open()) {
            // Schritt 1 simuliert nur eine einzelne Chappe-Station auf einer Linie
            channel.bind(new InetSocketAddress(0));
            channel.configureBlocking(true);

            // Die Nutzlast steht vorerst nur textuell für einen beobachteten Signalzustand (benutzen hier nio für easy Standardcharset)
            byte[] signalBytes = payload.getBytes(StandardCharsets.UTF_8);

            // Das UDP-Datagramm ersetzt hier nur technisch die optische Sichtverbindung
            ByteBuffer signalFrame = ByteBuffer.wrap(signalBytes);
            channel.send(signalFrame, target);
            System.out.printf("Signalzustand '%s' an Nachbarstation %s gesendet%n", payload, target);

            if (awaitReturnSignal) {
                waitForReturnSignal(channel, timeoutMillis);
            }
        }
    }

    private static void waitForReturnSignal(DatagramChannel channel, int timeoutMillis) throws IOException {
        // Polling spiegelt das periodische Beobachten eines Nachbarturms wider
        long deadline = System.currentTimeMillis() + timeoutMillis;
        ByteBuffer responseSignalFrame = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        channel.configureBlocking(false);

        try {
            while (System.currentTimeMillis() < deadline) {
                responseSignalFrame.clear();

                SocketAddress sender = channel.receive(responseSignalFrame);
                if (sender == null) {
                    pauseObservationLoop();
                    continue;
                }

                responseSignalFrame.flip();
                byte[] signalBytes = new byte[responseSignalFrame.remaining()];
                responseSignalFrame.get(signalBytes);

                String returnSignal = new String(signalBytes, StandardCharsets.UTF_8);
                System.out.printf("Rücksignal '%s' von Nachbarstation %s empfangen%n", returnSignal, sender);
                return;
            }
        } finally {
            channel.configureBlocking(true);
        }

        System.out.printf("Innerhalb von %d ms wurde kein Rücksignal von der Nachbarstation empfangen%n", timeoutMillis);
    }

    private static void pauseObservationLoop() {
        try {
            Thread.sleep(25L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printUsage() {
        System.out.println("Aufruf: ChappeStationClient <host> <port> <nutzlast> [--await-return-signal <timeoutMillis>]");
    }
}
