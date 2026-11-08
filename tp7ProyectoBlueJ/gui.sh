#!/bin/bash

# CLI launcher for the library GUI (Linux).
# Compiles the source into classes/ and starts the App with JavaFX + AtlantaFX + SQLite.
# Usage:  ./gui.sh      (run from tp7ProyectoBlueJ/)

# Exit immediately if any command fails
set -e

# Change directory to the current script's location
cd "$(dirname "$0")"

# Build the Classpath (CP) with required libraries
# Note: Linux uses a colon (:) as the classpath separator
CP="libs/javafx-base-21.0.13-linux.jar"
CP="$CP:libs/javafx-graphics-21.0.13-linux.jar"
CP="$CP:libs/javafx-controls-21.0.13-linux.jar"
CP="$CP:libs/javafx-fxml-21.0.13-linux.jar"
CP="$CP:libs/atlantafx-base-2.1.0.jar"
CP="$CP:libs/sqlite-jdbc-3.54.1.0.jar"

# Compile all Java source files into the 'classes' output directory
javac -cp "$CP" -d classes biblioteca/*.java biblioteca/persistencia/*.java biblioteca/gui/*.java

# Copy UI resources (FXML, images, CSS) to the compiled 'classes' directory
cp biblioteca/gui/*.fxml biblioteca/gui/*.png classes/biblioteca/gui/
cp biblioteca/gui/tema.css classes/biblioteca/gui/

# Add the compiled 'classes' directory to the beginning of the Classpath
CP="classes:$CP"

# Launch the Java application (exec replaces the current shell process with the Java process)
exec java -cp "$CP" biblioteca.gui.Launcher