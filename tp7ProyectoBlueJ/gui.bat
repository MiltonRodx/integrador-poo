@echo off
rem Lanzador CLI de la GUI de la biblioteca (Windows).
rem Compila en classes\ y arranca App con JavaFX + AtlantaFX + SQLite.
cd /d "%~dp0"

set CP=libs\javafx-base-21.0.13-win.jar
set CP=%CP%;libs\javafx-graphics-21.0.13-win.jar
set CP=%CP%;libs\javafx-controls-21.0.13-win.jar
set CP=%CP%;libs\javafx-fxml-21.0.13-win.jar
set CP=%CP%;libs\atlantafx-base-2.1.0.jar
set CP=%CP%;libs\sqlite-jdbc-3.54.1.0.jar

javac -cp "%CP%" -d classes biblioteca\*.java biblioteca\persistencia\*.java biblioteca\gui\*.java
copy /y biblioteca\gui\*.fxml classes\biblioteca\gui\ >nul
copy /y biblioteca\gui\*.png classes\biblioteca\gui\ >nul

set CP=classes;%CP%

java -cp "%CP%" biblioteca.gui.Launcher
