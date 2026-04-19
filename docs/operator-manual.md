# Operator-Handbuch für Schritt 2

## Grundidee

Ein Chappe-Operator "sendet" nicht direkt an eine entfernte Gegenstelle. Er beobachtet den Nachbarturm, übernimmt einen Zustand an seiner eigenen Mechanik und macht diesen Zustand damit für den nächsten Turm sichtbar.

Genau dieses Verhalten wird im Happy Path technisch so angenähert:

1. Eine Station zeigt lokal einen Signalzustand
2. Die nächste Relay-Station fragt diesen Zustand periodisch per UDP an
3. Nach einer kleinen Verzögerung übernimmt sie denselben Zustand lokal
4. Die folgende Station beobachtet wiederum diese neue lokale Anzeige

## Verhalten eines Relay-Operators

Der Relay-Operator arbeitet in der Simulation so:

1. Nachbarstation beobachten
2. Prüfen, ob sich der beobachtete Zustand geändert hat
3. Kurze Weitergabeverzögerung abwarten
4. Eigenen Turm auf denselben Zustand einstellen
5. Wieder beobachten

Das ist absichtlich monoton und mechanisch. Es geht hier nicht um "smarte" Verteilung, sondern um sichtbares Nachstellen eines Zustands entlang einer Linie.

## Verhalten von ENDPOINT_A

`ENDPOINT_A` spielt in Schritt 2 die sendende Ursprungsstation:

1. Anwendungssatz erzeugen
2. Satzschablonen in Zahlenpaare codieren
3. Die physische Folge `99 ... 00` erzeugen
4. Jeden Signalzustand nacheinander lokal einstellen

## Verhalten von ENDPOINT_B

`ENDPOINT_B` spielt in Schritt 2 die beobachtende Zielstation:

1. Vorherige Station periodisch beobachten
2. Startsignal `99` erkennen
3. Signalzustände sammeln
4. Endsignal `00` erkennen
5. Physische Folge dekodieren
6. Satzschablonen wieder in einen lesbaren Satz zurückführen

## Warum die Requests bidirektional wirken

Obwohl die eigentliche Nutznachricht im Happy Path von `ENDPOINT_A` nach `ENDPOINT_B` läuft, sind die UDP-Vorgänge selbst request/response-basiert:

- Eine Station fragt den sichtbaren Zustand der vorherigen Station aktiv an
- Die beobachtete Station antwortet mit ihrem aktuellen Zustand
- Der Relay stellt danach seinen eigenen Zustand lokal ein

Das passt zur historischen Idee, dass jede Station aktiv schauen musste, was der Nachbar gerade zeigt.
