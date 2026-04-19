# Chappe Telegraph

Dieses Repository implementiert das Projekt schrittweise. Schritt 1 beschränkt sich bewusst auf eine minimale UDP-Punkt-zu-Punkt-Verbindung als technische Basis für spätere Chappe-Turm-, Repeater- und Signalebenen.

Wichtig: Das Ziel ist hier nicht möglichst moderner UDP-Code, sondern eine verständliche Simulation des Chappe-Prinzips. Schritt 1 soll deshalb wie eine einfache Verbindung zwischen zwei benachbarten Stationen lesbar bleiben.

## Sprachkonvention

- Implementierung, Klassen- und Methodennamen auf Englisch
- Benutzerseitige Anwendungstexte auf Deutsch
- Dokumentation im Projektkontext überwiegend auf Deutsch

## Schritt 1

- Transport: UDP
- API-Basis: `java.nio` und `java.net`
- Kommunikationsform: Punkt-zu-Punkt zwischen zwei Prozessen
- Ablauf: bewusst blockierend und sequentiell wie das Beobachten und Weitergeben entlang einer Chappe-Linie
- Payload: einfacher UTF-8-Text als Platzhalter für einen Signalzustand
- Noch nicht enthalten: echte Armstellungen, Regulator-/Indikator-Codierung, Repeaterlogik, Baumtopologie, Zustandsmaschine, Fehlerkontrolle

## Warum so einfach?

- Eine lokale UDP-Station simuliert in Schritt 1 einen einzelnen Standort auf der Chappe-Linie
- Das Datagramm ist nur der technische Träger eines beobachteten Signalzustands
- Die Nutzlast steht vorerst textuell für das, was später ein Regulator-/Indikatorzustand wird
- Die lineare Verarbeitung macht das mechanische Weiterreichen sichtbar, statt es hinter moderner Nebenläufigkeit zu verstecken

## Schritt 1 starten

Projekt kompilieren:

```powershell
mvn -q -DskipTests compile
```

Server starten:

```powershell
java -cp target/classes de.chappe.telegraph.transport.udp.ChappeStationServer 9000
```

Server mit optionalem Rücksignal starten:

```powershell
java -cp target/classes de.chappe.telegraph.transport.udp.ChappeStationServer 9000 ACK
```

Client senden lassen:

```powershell
java -cp target/classes de.chappe.telegraph.transport.udp.ChappeStationClient 127.0.0.1 9000 SIGNAL-001
```

Client mit erwartetem Rücksignal:

```powershell
java -cp target/classes de.chappe.telegraph.transport.udp.ChappeStationClient 127.0.0.1 9000 SIGNAL-001 --await-return-signal 2000
```

## Schritt 2

Neue Bausteine:

- Anwendungsebene mit Satzschablonen
- Presentation Layer mit Mapping auf Zahlenpaare
- Physical Layer mit Start-/Endsignal und linearer Symbolfolge
- UDP-Happy-Path über `ENDPOINT_A`, `AB_RELAY_1`, `AB_RELAY_2`, `ENDPOINT_B`

Was Schritt 2 aktuell macht:

- `ENDPOINT_A` erzeugt einen kleinen Anwendungssatz
- Die Presentation Layer codiert ihn in Zahlenpaare
- Die Physical Layer macht daraus `99 10 11 12 13 00`
- Zwei Relay-Stationen beobachten jeweils ihren Vorderturm per UDP und übernehmen den Signalzustand
- `ENDPOINT_B` sammelt die Folge wieder ein und dekodiert sie zurück zum Satz

## Schritt 2 Schnellstart

Projekt kompilieren:

```powershell
mvn -q -DskipTests compile
```

Happy Path über die komplette Linie ausführen:

```powershell
java -cp target/classes de.chappe.telegraph.simulation.line.HappyPathLineDemo
```

Erwartetes Verhalten:

- In der Konsole erscheinen Logs von `ENDPOINT_A`, `AB_RELAY_1`, `AB_RELAY_2` und `ENDPOINT_B`
- `ENDPOINT_A` stellt nacheinander `99`, `10`, `11`, `12`, `13`, `00` ein
- Die Relay-Stationen übernehmen diese Zustände mit kleiner Verzögerung
- `ENDPOINT_B` meldet am Ende:
  `Physische Folge empfangen: 99 10 11 12 13 00`
  `Dekodierter Satz: ENDPOINT_A meldet sich. Übertragung bereit.`

## Schritt 2 manuell starten

Wenn du die Linie manuell statt über die Demo starten willst, brauchst du vier Prozesse.

`ENDPOINT_B` starten:

```powershell
java -cp target/classes de.chappe.telegraph.simulation.line.EndpointReceiverProcess ENDPOINT_B 9107 9108 127.0.0.1 9105 80 6
```

`AB_RELAY_2` starten:

```powershell
java -cp target/classes de.chappe.telegraph.simulation.line.RelayStationProcess AB_RELAY_2 9105 9106 127.0.0.1 9103 80 120
```

`AB_RELAY_1` starten:

```powershell
java -cp target/classes de.chappe.telegraph.simulation.line.RelayStationProcess AB_RELAY_1 9103 9104 127.0.0.1 9101 80 120
```

`ENDPOINT_A` starten:

```powershell
java -cp target/classes de.chappe.telegraph.simulation.line.EndpointSenderProcess ENDPOINT_A 9101 9102 500
```

Startreihenfolge:

1. `ENDPOINT_B`
2. `AB_RELAY_2`
3. `AB_RELAY_1`
4. `ENDPOINT_A`

## Bedeutung der Parameter

`EndpointSenderProcess`:

- `stationId`: Name der sendenden Station
- `displayPort`: Port, auf dem andere Stationen den sichtbaren Zustand abfragen
- `operatorPort`: Port, auf dem der lokale Zustand gesetzt wird
- `holdMillisPerSignal`: Wie lange ein Signalzustand sichtbar bleibt

`RelayStationProcess`:

- `stationId`: Name der Relay-Station
- `displayPort`: Sichtbarer Port dieser Station
- `operatorPort`: Port zum lokalen Einstellen des Zustands
- `observedHost`: Host der vorherigen Station
- `observedDisplayPort`: Display-Port der vorherigen Station
- `observationIntervalMillis`: Wie oft beobachtet wird
- `propagationDelayMillis`: Künstliche Verzögerung beim Übernehmen des Zustands

`EndpointReceiverProcess`:

- `stationId`: Name der Zielstation
- `displayPort`: Sichtbarer Port dieser Station
- `operatorPort`: Port zum lokalen Einstellen des Zustands
- `observedHost`: Host der vorherigen Station
- `observedDisplayPort`: Display-Port der vorherigen Station
- `observationIntervalMillis`: Wie oft beobachtet wird
- `maxSignals`: Maximale Anzahl erwarteter Signale inklusive Start und Ende

## Was in Schritt 2 noch fehlt

- Freie Satzwahl zur Laufzeit
- Größeres Codebuch für Satzschablonen
- Echte Chappe-Armstellungen statt Zahlenwerte
- Robuste Fehlerbehandlung bei verpassten Signalzuständen

## Maven

```powershell
mvn clean test
```

## SonarQube

- `sonar-project.properties` für lokale oder CI-Ausführung ist vorhanden

Beispiel:

```powershell
mvn verify sonar:sonar
```

## Doku

Passende Notizen liegen in:

- `docs/analogy-notes.md`
- `docs/layer-blueprint.md`
- `docs/operator-manual.md`
