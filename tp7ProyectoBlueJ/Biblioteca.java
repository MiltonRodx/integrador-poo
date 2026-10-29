// Clase Biblioteca.
// Alumnos a cargo de esta clase:
//   Ariel Sebastian Gomez 45940772,
//   Rodriguez Rivas Milton Nahuel 47896329,
//   Ruben Tiburcio Romero Fernandéz 46920161

import java.util.ArrayList;

public class Biblioteca {
    // -------------------
    // Atributos
    // -------------------
    private String nombre;
    private ArrayList<Libro> libros; // 0 a muchos
    private ArrayList<Socio> socios; // 0 a muchos
    
    public Biblioteca(String p_nombre, ArrayList<Libro> p_libros,
                ArrayList<Socio> p_socios) {
        // Verificación mínima a fin de que estén inicializados las colecciones.
        if (p_libros == null || p_socios == null) {
            this.setLibros(new ArrayList<>());
            this.setSocios(new ArrayList<>());
        }
        
        this.setNombre(p_nombre);
        this.setLibros(p_libros);
        this.setSocios(p_socios);
    }
    
    // Constructor inicializar en cero los libros y socios.
    public Biblioteca(String p_nombre) {
        this.setNombre(p_nombre);
        this.setLibros(new ArrayList<>());
        this.setSocios(new ArrayList<>());
    }

    // Metodos
    // Accessors
    
    private void setNombre(String p_nombre) { this.nombre = p_nombre; }
    
    public String getNombre() { return this.nombre; }
    
    // Libro
    private void setLibros(ArrayList<Libro> p_libros) { this.libros = p_libros; }
    
    public ArrayList<Libro> getLibros() { return this.libros; }
    
    // Socios
    private void setSocios(ArrayList<Socio> p_socios) { this.socios = p_socios; }
    
    public ArrayList<Socio> getSocios() { return this.socios; }
    
    // Metodos agregar y quitar 
    public boolean agregarLibro(Libro p_libro) { return this.getLibros().add(p_libro); }
    
    public boolean quitarLibro(Libro p_libro) { return this.getLibros().remove(p_libro); }
    
    public boolean agregarSocio(Socio p_socio) { return this.getSocios().add(p_socio); }
    
    public boolean quitarSocio(Socio p_socio) { return this.getSocios().remove(p_socio); }
    
    
    // Metodos especificos.
    // +nuevoLibro(p_titulo: String, p_edicion: int, p_editorial: String, p_anio: int): void
    public void nuevoLibro(String p_titulo, int p_edicion, String p_editorial, int p_anio) {
        // Crear un libro
        Libro tempLibro = new Libro(p_titulo, p_edicion, p_editorial, p_anio);
        
        // agregarlo a la colección actual
        boolean agregado = this.agregarLibro(tempLibro);
        
        if (!agregado)
            System.out.println("No se pudo agregar el libro " + tempLibro.getTitulo());
    }
    
    // +nuevoSocioEstudiante(p_dniSocio: int, p_nombre: String, p_carrera: String): void
    public void nuevoSocioEstudiante(int p_dniSocio, String p_nombre, String p_carrera) {
        // Creo un socio estudiante
        Socio tempEstudiante = new Estudiante(p_dniSocio, p_nombre, p_carrera);
        
        // lo agrego a los socios
        boolean agregado = this.agregarSocio(tempEstudiante);
        
        if (!agregado)
            System.out.println("No se pudo agregar el estudiante "
            + tempEstudiante.getDniSocio());
    }
    
    // +nuevoSocioDocente(p_dniSocio: int, p_nombre: String, p_area: String): void
    public void nuevoSocioDocente(int p_dniSocio, String p_nombre, String p_area) {
        // Creo socio docente
        Socio tempDocente = new Docente(p_dniSocio, p_nombre, p_area);
        
        boolean agregado = this.agregarSocio(tempDocente);
        
        if (!agregado)
            System.out.println("No se pudo agregar el docente "
            + tempDocente.getDniSocio());
    }
    
    // +prestarLibro(p_fechaRetiro: Calendar, p_socio: Socio, p_libro: Libro): boolean
    public boolean prestarLibro(Calendar p_fechaRetiro, Socio p_socio, Libro p_libro) {
        // que hay que hacer aca??
        // TODO
    }
    
    // +devolverLibro(p_libro: Libro): void
    public void devolverLibro(Libro p_libro) {
        // TODO
    }
    
    // +cantidadDeSociosPorTipo(p_objeto: String): int
    public int cantidadDeSociosPorTipo(String p_objeto) {
        // TODO
    }
    
    // +prestamosVencidos(): ArrayList
    public ArrayList prestamosVencidos() {
        // TODO
    }
    
    // +docentesResponsables(): ArrayList
    public ArrayList docentesResponsables() {
        // TODO
    }
    
    // +quienTieneElLibro(p_libro: Libro): String
    public String quienTieneElLibro(Libro p_libro) {
        // TODO
    }
    
    // +listaDeSocios(): String
    public String listaDeSocios() {
        // TODO
    }
    
    // +buscarSocio(p_dni: int): Socio
    public Socio buscarSocio(int p_dni) {
        // TODO
    }
    
    // +listaDeTitulos(): String
    public String listaDeTitulos() {
        // TODO
    }
    
    // +listaDeLibros(): String
    public String listaDeLibros() {
        // TODO
    }
    
    // +listaDeDocentesResponsables(): String
    public String listaDeDocentesResponsables() {
        // TODO
    }
    
    
}