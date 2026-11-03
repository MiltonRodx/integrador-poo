#!/bin/bash
# Lanzador CLI de la GUI de la biblioteca (Linux).
# Compila en classes/ y arranca App con JavaFX + AtlantaFX + SQLite.
# Uso:  ./gui.sh      (desde tp7ProyectoBlueJ/)
set -e
cd "$(dirname "$0")"

CP="libs/javafx-base-21.0.13-linux.jar"
CP="$CP:libs/javafx-graphics-21.0.13-linux.jar"
CP="$CP:libs/javafx-controls-21.0.13-linux.jar"
CP="$CP:libs/javafx-fxml-21.0.13-linux.jar"
CP="$CP:libs/atlantafx-base-2.1.0.jar"
CP="$CP:libs/sqlite-jdbc-3.54.1.0.jar"

javac -cp "$CP" -d classes biblioteca/*.java biblioteca/persistencia/*.java biblioteca/gui/*.java
cp biblioteca/gui/*.fxml biblioteca/gui/*.png classes/biblioteca/gui/

CP="classes:$CP"

exec java -cp "$CP" biblioteca.gui.Launcher
