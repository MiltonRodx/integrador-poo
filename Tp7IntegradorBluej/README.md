# Tp7IntegradorBluej

Proyecto integrador de programación orientada a objetos. Gestión de biblioteca con interfaz en Swing y persistencia en SQLite.

## Descripción

Sistema que permite gestionar la colección de libros y socios de una biblioteca, registrar préstamos y devoluciones, y consultar información sobre la misma. Implementado totalmente en Java con interfaz gráfica Swing y conexión a base de datos local.

## Estado actual

Los tests de validación superan la batería de requisitos de la consigna.

## Cómo usar

### Con BlueJ

1. Abrir BlueJ y crear un nuevo proyecto apuntando a la carpeta `Tp7IntegradorBluej/`.
2. BlueJ detectará automáticamente las clases en `biblioteca/`.
3. Compilar: clic derecho sobre el proyecto → `Compile Class`.
4. Ejecutar la consola: `GestionBiblioteca.main()`.
5. Ejecutar la GUI: `VentanaPrincipal.main()`.

### Con línea de comandos

```bash
# Compilar (ejemplo con javac y sqlite-jdbc)
javac -cp "+libs/sqlite-jdbc-3.54.1.0.jar" biblioteca/*.java -d classes
# Ejecutar consola
java -cp "classes:+libs/sqlite-jdbc-3.54.1.0.jar" biblioteca.GestionBiblioteca
# Ejecutar GUI
java -cp "classes:+libs/sqlite-jdbc-3.54.1.0.jar" biblioteca.VentanaPrincipal
```

### Desde el jar precompilado

```bash
java -jar mibibloteca.jar
```

## Estructura principal

- `biblioteca/` — Clases de dominio (Estudiante, Docente, Socio, Libro, Prestamo) y DAOs.
- `+libs/` — Dependencias externas (driver JDBC SQLite).
- `mibibloteca.jar` — Ejecutable con toda la interfaz Swing incluida.
- `biblioteca.db` — Base de datos local (no modificar directamente; las pruebas usan copia temporaria).

## Tecnologías

- Java JDK (versión del entorno).
- Swing para la interfaz gráfica (nativo, sin AtlantaFX ni JavaFX en el jar final).
- SQLite para persistencia local.

## Badge de estado

[![Tests OK](https://img.shields.io/badge/tests-25%2F25- green)](link-a-borrar-o-personalizar)

*(El badge de arriba forma parte del "capi pedorro" que solicitaste mantener; el enlace puede personalizarse o dejarse como referencia visual.)*