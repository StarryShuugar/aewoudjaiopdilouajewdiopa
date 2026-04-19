package de.chappe.telegraph.simulation.line;

import java.io.IOException;

public final class HappyPathLineDemo {

    private HappyPathLineDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        Thread endpointBThread = startThread("ENDPOINT_B", () -> EndpointReceiverProcess.main(new String[]{
                StationNodeId.ENDPOINT_B.name(),
                "9107",
                "9108",
                "127.0.0.1",
                "9105",
                "80",
                "6"
        }));

        Thread relayTwoThread = startThread("AB_RELAY_2", () -> RelayStationProcess.main(new String[]{
                StationNodeId.AB_RELAY_2.name(),
                "9105",
                "9106",
                "127.0.0.1",
                "9103",
                "80",
                "120"
        }));

        Thread relayOneThread = startThread("AB_RELAY_1", () -> RelayStationProcess.main(new String[]{
                StationNodeId.AB_RELAY_1.name(),
                "9103",
                "9104",
                "127.0.0.1",
                "9101",
                "80",
                "120"
        }));

        Thread.sleep(1_000L);

        Thread endpointAThread = startThread("ENDPOINT_A", () -> EndpointSenderProcess.main(new String[]{
                StationNodeId.ENDPOINT_A.name(),
                "9101",
                "9102",
                "500"
        }));

        endpointAThread.join();
        endpointBThread.join();

        relayOneThread.interrupt();
        relayTwoThread.interrupt();
    }

    private static Thread startThread(String name, ThrowingRunnable runnable) {
        Thread thread = new Thread(() -> {
            try {
                runnable.run();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException, InterruptedException;
    }
}
