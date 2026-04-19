package de.chappe.telegraph.simulation.line;

import java.io.IOException;
import java.util.Objects;

public final class RelayStationProcess {

    private RelayStationProcess() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 7) {
            printUsage();
            return;
        }

        StationNodeId stationId = StationNodeId.valueOf(args[0]);
        int displayPort = Integer.parseInt(args[1]);
        int operatorPort = Integer.parseInt(args[2]);
        String observedHost = args[3];
        int observedDisplayPort = Integer.parseInt(args[4]);
        int observationIntervalMillis = Integer.parseInt(args[5]);
        int propagationDelayMillis = Integer.parseInt(args[6]);

        TowerStationRuntime stationRuntime = new TowerStationRuntime(stationId, new StationPorts(displayPort, operatorPort));
        stationRuntime.start();

        UdpSignalClient udpSignalClient = new UdpSignalClient();
        String lastRelayedSignal = null;

        while (!Thread.currentThread().isInterrupted()) {
            String observedSignal = udpSignalClient.observeSignal(observedHost, observedDisplayPort);
            if (!Objects.equals(observedSignal, lastRelayedSignal)) {
                Thread.sleep(propagationDelayMillis);
                udpSignalClient.displaySignal("127.0.0.1", operatorPort, observedSignal);
                System.out.printf("[%s] Beobachteten Signalzustand %s übernommen%n", stationId, observedSignal);
                lastRelayedSignal = observedSignal;
            }
            Thread.sleep(observationIntervalMillis);
        }
    }

    private static void printUsage() {
        System.out.println("""
                Aufruf: RelayStationProcess <stationId> <displayPort> <operatorPort> \
                <observedHost> <observedDisplayPort> <observationIntervalMillis> <propagationDelayMillis>
                """.replace(System.lineSeparator(), " ").trim());
    }
}
