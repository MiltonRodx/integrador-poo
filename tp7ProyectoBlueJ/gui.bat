@echo off

rem CLI launcher for the library GUI (Windows).
rem Compiles the source into classes\ and starts the App with JavaFX + AtlantaFX + SQLite.

rem Change directory to the current script's location
cd /d "%~dp0"

rem Build the Classpath (CP) with required libraries
set CP=libs\javafx-base-21.0.13-win.jar
set CP=%CP%;libs\javafx-graphics-21.0.13-win.jar
set CP=%CP%;libs\javafx-controls-21.0.13-win.jar
set CP=%CP%;libs\javafx-fxml-21.0.13-win.jar
set CP=%CP%;libs\atlantafx-base-2.1.0.jar
set CP=%CP%;libs\sqlite-jdbc-3.54.1.0.jar

rem Compile all Java source files into the 'classes' output directory
javac -cp "%CP%" -d classes biblioteca\*.java biblioteca\persistencia\*.java biblioteca\gui\*.java

rem Copy UI resources (FXML, images, CSS) to the 'classes' directory silently
copy /y biblioteca\gui\*.fxml classes\biblioteca\gui\ >nul
copy /y biblioteca\gui\*.png classes\biblioteca\gui\ >nul
copy /y biblioteca\gui\tema.css classes\biblioteca\gui\ >nul

rem Add the compiled 'classes' directory to the beginning of the Classpath
set CP=classes;%CP%

rem Launch the Java application
java -cp "%CP%" biblioteca.gui.Launcher