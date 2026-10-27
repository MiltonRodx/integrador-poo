# 📚 Trabajo Integrador POO - Gestión de Biblioteca

¡Bienvenid@s al repositorio del **Trabajo Integrador de Programación Orientada a Objetos**! 

Este proyecto modela un sistema de **Gestión de Biblioteca** en Java, administrando el préstamo y devolución de libros para socios docentes y estudiantes, gestionando fechas de vencimiento, sanciones y reportes.

---

## 🖼️ Presentación del Proyecto
![Capibara Bibliotecario](./Capibara%20bibliotecario%20en%20la%20biblioteca%20acogedora.png)

---

## 🛠️ Estructura del Proyecto y División de Tareas

El desarrollo se encuentra organizado según la división de clases asignada al equipo de trabajo:

| Subgrupo | Integrantes | Clases Asignadas |
| :--- | :--- | :--- |
| **Grupo 1** | 1 Integrante | `Prestamo`, `Socio` |
| **Grupo 2** | 1 Integrante | `Estudiante`, `Docente` |
| **Grupo 3** | 1 Integrante | `Libro` |
| **Grupo 4** | 3 Integrantes | `Biblioteca` |
| **Equipo Completo** | Todos | `GestionBiblioteca` *(Clase ejecutable / Main)* |

---

## 🚀 Flujo Principal del Executable (`GestionBiblioteca`)

La clase ejecutable dentro de la rama `main` coordina los siguientes pasos clave:

1. **Instanciación:** Se crea la `Biblioteca` principal.
2. **Alta de Datos:** Carga inicial de ejemplares de `Libro` y socios de tipo `Docente` y `Estudiante`.
3. **Gestión de Préstamos:**
   - Registro de préstamos asociando `Socio`, `Libro` y la fecha de retiro mediante `Calendar`.
   - Control de reglas de negocio (límite de 3 libros para estudiantes y verificación de morosidad).
   - Devolución de libros actualizando la fecha y calculando docentes responsables.
4. **Emisión de Reportes:**
   - Cantidad de socios por tipo.
   - Listado de libros, socios y títulos únicos.
   - Consulta de morosidad y búsqueda del socio poseedor de un libro específico.

---

## 💻 Tecnologías Utilizadas

- **Lenguaje:** Java 
- **Entorno de Desarrollo:** BlueJ
- **Control de Versiones:** Git & GitHub
