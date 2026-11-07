package biblioteca.gui;

/** Los numeros que muestra el panel de estadisticas. */
public record EstadisticasBiblioteca(
        int totalLibros,
        int librosDisponibles,
        int totalSocios,
        int sociosDocentes,
        int sociosEstudiantes,
        int prestamosActivos,
        int prestamosVencidos) {
}
