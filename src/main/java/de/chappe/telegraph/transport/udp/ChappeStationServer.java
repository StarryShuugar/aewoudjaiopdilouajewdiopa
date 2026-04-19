package de.chappe.telegraph.transport.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public final class ChappeStationServer {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private ChappeStationServer() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            printUsage();
            return;
        }

        int port = Integer.parseInt(args[0]);
        String returnSignal = args.length == 2 ? args[1] : null;

        try (DatagramChannel channel = DatagramChannel.open()) {
            // Diese Station simuliert einen einzelnen Chappe-Turm auf einer Linie
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(true);

            System.out.printf("Turmstation wartet auf UDP-Port %d für eingehende Signalzustände%n", port);

            // Der Ablauf bleibt bewusst linear: beobachten, lesen und optional bestätigen
            ByteBuffer receiveBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            SocketAddress sender = channel.receive(receiveBuffer);
            receiveBuffer.flip();

            byte[] signalBytes = new byte[receiveBuffer.remaining()];
            receiveBuffer.get(signalBytes);

            String payload = new String(signalBytes, StandardCharsets.UTF_8);
            System.out.printf("Signalzustand '%s' von Nachbarstation %s empfangen%n", payload, sender);

            if (returnSignal != null) {
                // Das Rücksignal ist nur eine kleine Bestätigung für Schritt 1
                ByteBuffer returnSignalFrame = ByteBuffer.wrap(returnSignal.getBytes(StandardCharsets.UTF_8));
                channel.send(returnSignalFrame, sender);
                System.out.printf("Rücksignal '%s' an Nachbarstation %s gesendet%n", returnSignal, sender);
            }
        }
    }

    private static void printUsage() {
        System.out.println("Aufruf: ChappeStationServer <port> [rücksignal]");
    }
}
