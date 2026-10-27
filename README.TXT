# Trabajo Integrador de Programación Orientada a Objetos - Gestión de Biblioteca

<p align="center">
  <img src="./portada-biblioteca.png" alt="Capibara bibliotecario" width="850">
</p>

## Descripción del proyecto

Este proyecto consiste en el desarrollo de un sistema de gestión de biblioteca en Java, realizado para la asignatura Programación Orientada a Objetos.

El sistema permite administrar libros, socios y préstamos, contemplando las diferencias entre docentes y estudiantes. También incluye el control de fechas de vencimiento, devoluciones, morosidad y la generación de reportes.

## Organización del proyecto

El desarrollo se encuentra organizado según la división de clases asignada a los integrantes del equipo.

| Subgrupo | Integrantes | Clases asignadas |
|---|---|---|
| Grupo 1 | 1 integrante | `Prestamo`, `Socio` |
| Grupo 2 | 1 integrante | `Estudiante`, `Docente` |
| Grupo 3 | 1 integrante | `Libro` |
| Grupo 4 | 3 integrantes | `Biblioteca` |
| Equipo completo | Todos | `GestionBiblioteca` (clase ejecutable) |

## Funcionamiento del sistema

La clase `GestionBiblioteca` coordina las principales operaciones del programa:

1. Creación de la biblioteca y carga inicial de libros y socios.
2. Registro de docentes y estudiantes.
3. Gestión de préstamos y devoluciones mediante fechas.
4. Control del límite de préstamos de los estudiantes y verificación de morosidad.
5. Consulta de libros, socios y títulos únicos.
6. Generación de reportes y búsqueda de socios que tienen libros prestados.

## Tecnologías utilizadas

- Java: desarrollo del sistema.
- BlueJ: entorno de desarrollo.
- Git: control de versiones.
- GitHub: alojamiento del repositorio y trabajo colaborativo.

## Conceptos aplicados

Durante el desarrollo se trabajan conceptos de programación orientada a objetos, como clases, objetos, encapsulamiento, herencia, polimorfismo, asociaciones entre objetos y colecciones.

También se utiliza `Calendar` para gestionar las fechas relacionadas con los préstamos y las devoluciones.
