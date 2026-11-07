#!/bin/bash
# Lanzador CLI de la GUI de la biblioteca (Linux).
# Compila en classes/ y arranca AppSwing con Swing nativo del JDK + SQLite.
# Sin JavaFX, sin AtlantaFX, sin modules: solo el driver sqlite-jdbc.
# Uso:  ./gui.sh      (desde tp7ProyectoBlueJ/)
set -e
cd "$(dirname "$0")"

CP="libs/sqlite-jdbc-3.54.1.0.jar"

javac -cp "$CP" -d classes biblioteca/*.java biblioteca/persistencia/*.java biblioteca/swing/*.java

CP="classes:$CP"

exec java -cp "$CP" biblioteca.swing.AppSwing
