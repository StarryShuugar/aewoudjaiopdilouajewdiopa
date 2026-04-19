package de.chappe.telegraph.simulation.line;

import java.io.IOException;

import de.chappe.telegraph.layer.application.ApplicationLayerService;
import de.chappe.telegraph.layer.application.ApplicationSentence;
import de.chappe.telegraph.layer.physical.PhysicalServiceAccessPoint;
import de.chappe.telegraph.layer.physical.PhysicalSignalService;
import de.chappe.telegraph.layer.physical.SignalCode;
import de.chappe.telegraph.layer.physical.SignalSequence;
import de.chappe.telegraph.layer.presentation.PresentationServiceAccessPoint;
import de.chappe.telegraph.layer.presentation.TemplatePairPresentationService;

public final class EndpointSenderProcess {

    private EndpointSenderProcess() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 4) {
            printUsage();
            return;
        }

        StationNodeId stationId = StationNodeId.valueOf(args[0]);
        int displayPort = Integer.parseInt(args[1]);
        int operatorPort = Integer.parseInt(args[2]);
        int holdMillisPerSignal = Integer.parseInt(args[3]);

        TowerStationRuntime stationRuntime = new TowerStationRuntime(stationId, new StationPorts(displayPort, operatorPort));
        stationRuntime.start();

        ApplicationLayerService applicationLayerService = new ApplicationLayerService();
        PresentationServiceAccessPoint presentationService = new TemplatePairPresentationService();
        PhysicalServiceAccessPoint physicalService = new PhysicalSignalService();
        UdpSignalClient udpSignalClient = new UdpSignalClient();

        ApplicationSentence sentence = applicationLayerService.createHappyPathSentence();
        SignalSequence signalSequence = physicalService.encode(presentationService.encode(sentence));

        System.out.printf("[%s] Anwendungssatz: %s%n", stationId, sentence.text());
        System.out.printf("[%s] Physische Folge: %s%n", stationId, signalSequence.asWireString());

        for (SignalCode signalCode : signalSequence.codes()) {
            udpSignalClient.displaySignal("127.0.0.1", operatorPort, signalCode.asTwoDigitString());
            System.out.printf("[%s] Signalzustand %s lokal eingestellt%n", stationId, signalCode.asTwoDigitString());
            Thread.sleep(holdMillisPerSignal);
        }
    }

    private static void printUsage() {
        System.out.println("Aufruf: EndpointSenderProcess <stationId> <displayPort> <operatorPort> <holdMillisPerSignal>");
    }
}
