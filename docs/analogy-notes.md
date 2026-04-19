# Analogie-Notizen

## Chappe-Telegraph als "mechanical internet"

Der Chappe-Telegraph war ein optisches Netz aus Stationen beziehungsweise Türmen, die sich gegenseitig per Teleskop beobachteten. Ein Operator las den Signalzustand des vorherigen Turms ab, stellte denselben Zustand an seiner eigenen Mechanik ein und gab ihn so an die nächste Station weiter.

Nach [Wikipedia](https://en.wikipedia.org/wiki/Chappe_telegraph) standen die Türme typischerweise etwa 5 bis 15 Kilometer auseinander. Die Mechanik bestand aus einem Mast, einem Regulator und zwei Indikatoren. Signale wurden also nicht als elektrische Impulse, sondern als sichtbare Zustände einer mechanischen Konstruktion übertragen.

## Bezug zum Physical Layer

Der [Physical Layer](https://en.wikipedia.org/wiki/Physical_layer) definiert die Übertragung von Bits oder Symbolen über ein Medium und beschreibt die mechanische, elektrische oder prozedurale Schnittstelle dieses Mediums. Genau diese Sicht hilft hier:

- Medium: historisch Sichtlinie und Luft, im Projekt zunächst UDP zwischen Prozessen
- Signal: historisch Regulator- und Indikatorstellung, im Projekt zunächst textuell codierter Signalzustand
- Stationen: historisch Türme mit Operatoren, im Projekt später Prozesse mit klaren Rollen
- Weitergabe: historisch Beobachten und Nachstellen, im Projekt später Empfangen und Weiterleiten

## Schritt 1

Bevor wir echte Armstellungen, Vokabularbuch, Baumtopologie oder Repeater einführen, brauchen wir zuerst einen stabilen station-zu-station Transportkanal. Deshalb prüft Schritt 1 nur:

- Eine Station kann einen Signalzustand senden
- Eine benachbarte Station kann ihn empfangen
- Optional kann ein kleines Rücksignal gesendet werden

## Didaktische Leitidee

Schritt 1 soll sich nicht wie ein gewöhnliches UDP-Beispiel anfühlen, sondern wie die kleinste technische Simulation einer Chappe-Linie:

- Ein Prozess repräsentiert eine Station auf der Linie
- Ein Datagramm steht für einen übertragenen Signalzustand
- Der Polling-Ablauf auf Empfängerseite erinnert an das periodische Beobachten des Nachbarturms
- Die einfache Punkt-zu-Punkt-Struktur ist bewusst gewählt, weil das Projekt später auf benachbarte Türme, Repeater und Linienlogik aufbaut

Wenn der Code in diesem Stadium zu generisch, zu abstrakt oder zu modern wirkt, verliert man leicht den Bezug zur historischen Architektur. Für dieses Projekt ist Verständlichkeit des Chappe-Prinzips wichtiger als maximale technische Eleganz.

## Quellen

- [Chappe telegraph (Wikipedia EN)](https://en.wikipedia.org/wiki/Chappe_telegraph)
- [Semaphore Telegraph (IEEE REACH)](https://reach.ieee.org/primary-sources/semaphore-telegraph/)
- [Physical layer (Wikipedia EN)](https://en.wikipedia.org/wiki/Physical_layer)

## Schritt 2: Layer-Modell und Internet-Analogien

Mit Schritt 2 wird die mechanische Analogie feiner auf moderne Layer-Eigenschaften abgebildet.

### OSI-Modell-Entsprechungen

| Schicht | Chappe-Welt | Internet | Implementierung |
|---------|------------|---------|-----------------|
| **7. Application** | Bedeutung des Satzes | HTTP, SMTP, DNS | `SentenceTemplate` enum |
| **6. Presentation** | Übersetzung in Paaraktionen | Unicode, Datenformat | `TemplatePairPresentationService` (Mapping) |
| **5. Session** | (noch nicht) | TCP handshake | Geplant: Verbindungsaufbau |
| **4. Transport** | (noch nicht) | TCP, UDP | Aktuell direkt UDP |
| **3. Network** | Repeater-Logik | IP, Routing | Geplant: Netzwerk-Topologie |
| **2. Data Link** | (noch nicht) | Ethernet, CRC | Geplant: Fehlerkorrektur |
| **1. Physical** | Sichtbare Armstellungen | Spannung, Lichtwellen | `PhysicalSignalService` + UDP-Transport |

### Funktionale Trennung (Step 2)

**Application Layer** (`SentenceTemplate`):
- Bedeutungsebene: "ENDPOINT_A_ANNOUNCES" ist ein **semantischer Ausdruck**
- Unabhängig davon, wie er technisch übertragen wird
- Analog zu: HTTP-Request ist unabhängig von TCP-Details

**Presentation Layer** (`TemplatePairPresentationService`):
- **Codec-Ebene**: Abbildung auf gemeinsam verstandene Symbolpaare
- Maps SentenceTemplate → SignalPair: (10,11) bedeutet "Endpunkt A zeigt sich"
- Analog zu: Base64-Encoding, Bildkompression, oder Charsets
- **Wichtig**: Diese Paare sind nicht zufällig - sie sind das gemeinsame "Vokabular" zwischen allen Stationen

**Physical Layer** (`PhysicalSignalService` + UDP):
- **Übertragungsformat**: Folge von zweistelligen Codes
- Struktur: `99 [first] [second] 00` = "Start-Marker, Daten, End-Marker"
- Analog zu: Bitrate, Modulation, Framing in modernem Internet
- Analog zum Chappe: Die genaue **Bewegungsabfolge** der Arme ist das "Signal auf dem Kabel"

### Operator-Verhalten als Layer-Kommunikation

Der Polling-Mechanismus in `RelayStationProcess` ist **keine ineffiziente Lösung**, sondern die mechanische Entsprechung moderner SAP (Service Access Points):

```
Relay beobachtet Vorgänger:
1. observeSignal(host, port) auf port des Vorgängers
2. Polling-Intervall = "Ich schaue alle 80ms nach"
3. Verzögerung = "Zeitverzug bis ich mein Teleskop justiert habe"
4. displaySignal(localhost, operatorPort) = "Ich stelle an meinem Turm das nach, was ich sah"
```

Dies ist funktional identisch mit:
- **Request-Response Pattern**: Relay fragt den Vorgänger ab
- **Latenz-Simulation**: Verzögerungen zwischen den Türmen
- **State Machine**: Relay merkt sich `lastRelayedSignal`, um Duplikate zu vermeiden

### Warum noch kein "echtes" OSI-Layer-3 und -2?

- **Netzwerk-Layer (3)**: Erst nötig, wenn **mehrere isolierte Linien** verbunden werden
  - Im historischen Chappe: Jede Linie war eigenständig
  - Im Projekt: Momentan nur eine lineare A-Relay1-Relay2-B Kette
  
- **Data-Link / CRC (2)**: Fehlerbehandlung ist noch nicht nötig, weil wir auf `localhost` über UDP testen
  - Im historischen Chappe: Fehler = Operator macht Lesefehler, müsste fragen "War das ein 30er oder 31er?"
  - Im Projekt: Könnte später mit Parity-Bits oder Wiederholung realisiert werden

### Bidirektionale Kommunikation

Das Design mit zwei UDP-Ports pro Station ist **bewusst gewählt**:

**displayPort** (Sender):
- Der Turm "zeigt" seinen aktuellen Zustand
- Analog zu: Leuchtfeuer, das von anderen beobachtet wird
- In modernem Internet: Der Server, der auf Port 80 horcht

**operatorPort** (Beobachter):
- Der Operator "beobachtet" den Vorgänger
- Analog zu: Teleskop auf den nächsten Turm richten
- In modernem Internet: Der Client, der eine HTTP-Anfrage macht

Dies ist **nicht** Vollduplex (simultan bidirektional), sondern **Halbduplex mit expliziter Rollenteilung**:
- Relay beobachtet (operatorPort) → ruft ab
- Relay zeigt (displayPort) ← wird abgerufen

Dies entspricht dem historischen Ablauf: Ein Operator konnte nicht gleichzeitig seinen Turm bedienen und den Vorgänger beobachten.

## Quellen

- [OSI Model (Cisco)](https://www.cisco.com/c/en/us/support/docs/security/asa-5500-x-series-next-generation-firewalls/119774-layer-model-osi-00.html)
- [Chappe telegraph (Wikipedia EN)](https://en.wikipedia.org/wiki/Chappe_telegraph)
- [Semaphore Telegraph (IEEE REACH)](https://reach.ieee.org/primary-sources/semaphore-telegraph/)
- [Physical layer (Wikipedia EN)](https://en.wikipedia.org/wiki/Physical_layer)
