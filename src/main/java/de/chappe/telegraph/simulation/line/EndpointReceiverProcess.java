package de.chappe.telegraph.simulation.line;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.chappe.telegraph.layer.application.ApplicationSentence;
import de.chappe.telegraph.layer.physical.PhysicalServiceAccessPoint;
import de.chappe.telegraph.layer.physical.PhysicalSignalService;
import de.chappe.telegraph.layer.physical.SignalCode;
import de.chappe.telegraph.layer.physical.SignalSequence;
import de.chappe.telegraph.layer.presentation.PresentationServiceAccessPoint;
import de.chappe.telegraph.layer.presentation.SignalPair;
import de.chappe.telegraph.layer.presentation.TemplatePairPresentationService;

public final class EndpointReceiverProcess {

    private EndpointReceiverProcess() {
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
        int maxSignals = Integer.parseInt(args[6]);

        TowerStationRuntime stationRuntime = new TowerStationRuntime(stationId, new StationPorts(displayPort, operatorPort));
        stationRuntime.start();

        UdpSignalClient udpSignalClient = new UdpSignalClient();
        PhysicalServiceAccessPoint physicalService = new PhysicalSignalService();
        PresentationServiceAccessPoint presentationService = new TemplatePairPresentationService();

        List<SignalCode> collectedSignals = new ArrayList<>();
        String lastObservedSignal = null;
        boolean messageStarted = false;

        while (collectedSignals.size() < maxSignals) {
            String observedSignal = udpSignalClient.observeSignal(observedHost, observedDisplayPort);
            if (!observedSignal.equals(lastObservedSignal)) {
                udpSignalClient.displaySignal("127.0.0.1", operatorPort, observedSignal);

                if ("99".equals(observedSignal)) {
                    messageStarted = true;
                    collectedSignals.clear();
                    collectedSignals.add(new SignalCode(99));
                    System.out.printf("[%s] Startsignal 99 beobachtet%n", stationId);
                } else if (messageStarted) {
                    int numericSignal = Integer.parseInt(observedSignal);
                    collectedSignals.add(new SignalCode(numericSignal));
                    System.out.printf("[%s] Signalzustand %s übernommen%n", stationId, observedSignal);
                    if ("00".equals(observedSignal)) {
                        break;
                    }
                }
            }

            lastObservedSignal = observedSignal;
            Thread.sleep(observationIntervalMillis);
        }

        SignalSequence signalSequence = new SignalSequence(collectedSignals);
        List<SignalPair> signalPairs = physicalService.decode(signalSequence);
        ApplicationSentence sentence = presentationService.decode(signalPairs);

        System.out.printf("[%s] Physische Folge empfangen: %s%n", stationId, signalSequence.asWireString());
        System.out.printf("[%s] Dekodierter Satz: %s%n", stationId, sentence.text());
    }

    private static void printUsage() {
        System.out.println("""
                Aufruf: EndpointReceiverProcess <stationId> <displayPort> <operatorPort> \
                <observedHost> <observedDisplayPort> <observationIntervalMillis> <maxSignals>
                """.replace(System.lineSeparator(), " ").trim());
    }
}
