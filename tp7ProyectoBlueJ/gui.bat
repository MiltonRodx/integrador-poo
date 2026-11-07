@echo off
rem Lanzador CLI de la GUI de la biblioteca (Windows).
rem Compila en classes\ y arranca AppSwing con Swing nativo del JDK + SQLite.
rem Sin JavaFX, sin AtlantaFX, sin modules: solo el driver sqlite-jdbc.
cd /d "%~dp0"

set CP=libs\sqlite-jdbc-3.54.1.0.jar

javac -cp "%CP%" -d classes biblioteca\*.java biblioteca\persistencia\*.java biblioteca\swing\*.java

set CP=classes;%CP%

java -cp "%CP%" biblioteca.swing.AppSwing
