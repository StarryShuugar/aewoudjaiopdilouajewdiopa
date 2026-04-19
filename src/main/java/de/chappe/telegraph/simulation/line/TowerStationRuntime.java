package de.chappe.telegraph.simulation.line;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public final class TowerStationRuntime {

    private static final int BUFFER_SIZE = 1024;

    private final StationNodeId stationId;
    private final StationPorts stationPorts;
    private final SignalDisplayState signalDisplayState;

    public TowerStationRuntime(StationNodeId stationId, StationPorts stationPorts) {
        this.stationId = stationId;
        this.stationPorts = stationPorts;
        this.signalDisplayState = new SignalDisplayState();
    }

    public void start() {
        Thread displayThread = new Thread(this::runDisplayService, stationId + "-display-service");
        displayThread.setDaemon(true);
        displayThread.start();

        Thread operatorThread = new Thread(this::runOperatorService, stationId + "-operator-service");
        operatorThread.setDaemon(true);
        operatorThread.start();
    }

    private void runDisplayService() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(stationPorts.displayPort()));
            channel.configureBlocking(true);

            while (!Thread.currentThread().isInterrupted()) {
                ByteBuffer requestBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                SocketAddress requester = channel.receive(requestBuffer);
                requestBuffer.flip();

                String request = decode(requestBuffer);
                String response = UdpStationProtocol.handleDisplayRequest(request, signalDisplayState.currentSignal());
                channel.send(encode(response), requester);
            }
        } catch (IOException ignored) {
        }
    }

    private void runOperatorService() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(stationPorts.operatorPort()));
            channel.configureBlocking(true);

            while (!Thread.currentThread().isInterrupted()) {
                ByteBuffer requestBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                SocketAddress requester = channel.receive(requestBuffer);
                requestBuffer.flip();

                String request = decode(requestBuffer);
                String response = UdpStationProtocol.handleOperatorRequest(request, signalDisplayState);
                channel.send(encode(response), requester);
            }
        } catch (IOException ignored) {
        }
    }

    private static ByteBuffer encode(String text) {
        return ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
