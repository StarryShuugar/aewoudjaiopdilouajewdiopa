# Layer-Blueprint für Schritt 2

## Ziel dieses Schritts

Schritt 2 zieht die ersten drei Ebenen bewusst als kleine, verständliche Pipeline ein:

1. Anwendungsebene: Ein sinnvoller Satz aus Satzschablonen
2. Presentation Layer: Abbildung der Satzschablonen auf numerische Paare
3. Physical Layer: Übertragung einer linearen Signalsequenz mit Start- und Endsignal

Die Relay-Stationen verhalten sich dabei nicht wie moderne Router, sondern wie Chappe-Türme: beobachten, übernehmen, weiterreichen.

## Layer und Aufgaben

### Anwendungsebene

Zweck:

- Formuliert die Bedeutung der Nachricht
- Arbeitet mit Satzschablonen statt mit nackten Zahlen
- Hält die Kommunikation für Menschen lesbar

Aktueller Stand:

- `ENDPOINT_A meldet sich.`
- `Übertragung bereit.`

Diese beiden Satzschablonen bilden im Happy Path zusammen den ersten Beispielsatz.

### Presentation Layer

Zweck:

- Übersetzt Satzschablonen in gemeinsam verstandene Codes
- Macht aus einem semantischen Satz eine transportierbare Darstellung
- Entschlüsselt die Paare auf der Empfängerseite wieder zurück in Satzschablonen

Aktueller Stand:

- `ENDPOINT_A meldet sich.` -> `(10,11)`
- `Übertragung bereit.` -> `(12,13)`

Damit ist die Presentation Layer im Moment eine kleine Codebuch-Instanz.

### Physical Layer

Zweck:

- Überträgt die Symbolfolge zwischen benachbarten Stationen
- Führt Start- und Endsignal ein
- Simuliert die Sichtlinie der Chappe-Türme mit UDP und periodischer Beobachtung

Aktueller Stand:

- Startsignal: `99`
- Nutzdaten: `10 11 12 13`
- Endsignal: `00`

Komplette Folge:

`99 10 11 12 13 00`

## Service Access Points

Die SAPs sind hier zunächst bewusst klein und direkt im Code sichtbar:

- Anwendung -> Presentation:
  `PresentationServiceAccessPoint.encode(ApplicationSentence)`
- Presentation -> Anwendung:
  `PresentationServiceAccessPoint.decode(List<SignalPair>)`
- Presentation -> Physical:
  `PhysicalServiceAccessPoint.encode(List<SignalPair>)`
- Physical -> Presentation:
  `PhysicalServiceAccessPoint.decode(SignalSequence)`

Die UDP-Ports der Stationen sind etwas anderes: Sie sind keine Layer-SAPs, sondern die technische Schnittstelle der simulierten Turmstation.

## Stationsmodell

Jede Station besitzt in Schritt 2 zwei UDP-Ports:

- Display-Port:
  Hier fragt die nächste Station den aktuell sichtbaren Signalzustand ab.
- Operator-Port:
  Hier stellt der lokale Operator den Signalzustand der Station ein.

Damit entsteht bewusst eine kleine Trennung zwischen:

- beobachten
- lokal einstellen
- weitergeben

## Moderne Internet-Analogie

Die Analogie zum modernen Internet ist absichtlich nicht 1:1 elektrisch, sondern funktional:

- Anwendungsebene:
  Bedeutung der Nachricht, ähnlich der Fachsemantik einer Anwendung
- Presentation Layer:
  gemeinsame Codierung und Dekodierung
- Physical Layer:
  tatsächliche Übertragung von Symbolen über ein Medium
- Relay-Station:
  ähnlich einem Regenerator oder Repeater auf Schicht 1, nicht einem Router auf Schicht 3

## Aktuelle Grenzen

- Der Happy Path ist momentan auf eine feste Linie ausgelegt
- Der Satzschablonen-Katalog ist noch sehr klein
- Es gibt noch keine Fehlerbehandlung für verlorene oder verpasste Signalzustände
- Die Relay-Logik arbeitet noch als einfache Nachstell- und Beobachtungslogik ohne komplexe Betriebszustände
