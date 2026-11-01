package biblioteca;

// Clase Biblioteca.
// Alumnos a cargo de esta clase:
//   Ariel Sebastian Gomez 45940772,
//   Rodriguez Rivas Milton Nahuel 47896329,
//   Ruben Tiburcio Romero Fernandéz 46920161

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;

public class Biblioteca {
    // -------------------
    // Atributos
    // -------------------
    private String nombre;
    private ArrayList<Libro> libros; // relacion 0 a muchos.
    private ArrayList<Socio> socios; // relacion 0 a muchos también.
    
    // -------------------
    // Constructores
    // -------------------
    public Biblioteca(String p_nombre, ArrayList<Libro> p_libros,
                ArrayList<Socio> p_socios) {
        if (p_libros == null || p_socios == null) {
            this.setLibros(new ArrayList<>());
            this.setSocios(new ArrayList<>());
        } else {
            this.setLibros(p_libros);
            this.setSocios(p_socios);
        }
        
        this.setNombre(p_nombre);
    }
    
    // Constructor inicializar en cero los libros y socios.
    public Biblioteca(String p_nombre) {
        this.setNombre(p_nombre);
        this.setLibros(new ArrayList<>());
        this.setSocios(new ArrayList<>());
    }

    // ---------------
    // Accessors
    // ---------------    
    private void setNombre(String p_nombre) { this.nombre = p_nombre; }
    
    public String getNombre() { return this.nombre; }
    
    private void setLibros(ArrayList<Libro> p_libros) { this.libros = p_libros; }
    
    public ArrayList<Libro> getLibros() { return this.libros; }
    
    private void setSocios(ArrayList<Socio> p_socios) { this.socios = p_socios; }
    
    public ArrayList<Socio> getSocios() { return this.socios; }
    
    public boolean agregarLibro(Libro p_libro) { return this.getLibros().add(p_libro); }
    
    public boolean quitarLibro(Libro p_libro) { return this.getLibros().remove(p_libro); }
    
    public boolean agregarSocio(Socio p_socio) { return this.getSocios().add(p_socio); }
    
    public boolean quitarSocio(Socio p_socio) { return this.getSocios().remove(p_socio); }
    
    // ------------------------
    // Metodos especificos.
    // ------------------------
    
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
        // Creo un socio estudiante; la consigna fija 20 días de préstamo (vence "20 días después del retiro").
        Socio tempEstudiante = new Estudiante(p_dniSocio, p_nombre, 20, p_carrera);
        
        // lo agrego a los socios
        boolean agregado = this.agregarSocio(tempEstudiante);
        
        if (!agregado)
            System.out.println("No se pudo agregar el estudiante "
            + tempEstudiante.getDniSocio());
    }
    
    // +nuevoSocioDocente(p_dniSocio: int, p_nombre: String, p_area: String): void
    public void nuevoSocioDocente(int p_dniSocio, String p_nombre, String p_area) {
        // Creo socio docente; la consigna fija 5 días de préstamo iniciales (luego esResponsable los premia).
        Socio tempDocente = new Docente(p_dniSocio, p_nombre, 5, p_area);
        
        boolean agregado = this.agregarSocio(tempDocente);
        
        if (!agregado)
            System.out.println("No se pudo agregar el docente "
            + tempDocente.getDniSocio());
    }
    

    // +prestarLibro(p_fechaRetiro: Calendar, p_socio: Socio, p_libro: Libro): boolean
    public boolean prestarLibro(Calendar p_fechaRetiro, Socio p_socio, Libro p_libro) {
        // Crea el préstamo y lo agrega al socio y al libro.
        // Rechazo silencioso (return false): si falta alguna de las tres piezas, no hay préstamo que hacer.
        if (p_fechaRetiro == null || p_socio == null || p_libro == null) return false;

        // Verificamos primero que el libro no esté prestado.
        if (p_libro.prestado()) return false;

        // El socio decide si puede pedir (tope de libros / no debe préstamos vencidos).
        if (!p_socio.puedePedir()) return false;

        // Creamos un préstamo.
        Prestamo tempPrestamo = new Prestamo(p_fechaRetiro, p_socio, p_libro);

        // Lo agregamos al socio; si falla, no hay nada que deshacer.
        if (!p_socio.agregarPrestamo(tempPrestamo)) return false;

        // Lo agregamos al libro; si falla, deshacemos lo hecho en el socio.
        if (!p_libro.agregarPrestamo(tempPrestamo)) {
            p_socio.quitarPrestamo(tempPrestamo);
            return false;
        }

        return true;
    }
    
    // +devolverLibro(p_libro: Libro): void
    public void devolverLibro(Libro p_libro) throws LibroNoPrestadoException {
        // devolverLibro(Libro): Asigna la fecha de devolución del préstamo con la fecha actual.
        // Si el libro no se encuentra prestado lanzar una excepción de tipo LibroNoPrestadoException
        // con el mensaje descriptivo “El libro <<titulo>> no se puede devolver
        // ya que se encuentra en la biblioteca”
        
        Calendar fechaActual = Calendar.getInstance(); // Obtenemos fecha local.
        
        // Verificar que el libro no se encuentre prestado y largar excepcion.
        if (!p_libro.prestado()) {
            throw new LibroNoPrestadoException("El libro " + p_libro.getTitulo()
                + " no se puede devolver ya que se encuentra en la biblioteca.");
        }
        
        p_libro.ultimoPrestamo().registrarFechaDevolucion(fechaActual);
    }
    
    // +cantidadDeSociosPorTipo(p_objeto: String): int
    public int cantidadDeSociosPorTipo(String p_objeto) {
        return (int) this.getSocios().stream()
            .filter(p -> p.soyDeLaClase().equalsIgnoreCase(p_objeto)).count();
    }
    
    // +prestamosVencidos(): ArrayList
    public ArrayList<Prestamo> prestamosVencidos() {
        // devuelve una colección con los préstamos ACTIVOS (aún sin devolver) vencidos al día de la fecha.
        // evaluación a nivel día: hoy se normaliza a medianoche (lo que vence HOY todavía no está vencido).
        Calendar fechaHoy = new GregorianCalendar();
        
        // limpieza de hora minuto segundo y milisegundo
        fechaHoy.set(Calendar.HOUR_OF_DAY, 0);
        fechaHoy.set(Calendar.MINUTE, 0);
        fechaHoy.set(Calendar.SECOND, 0);
        fechaHoy.set(Calendar.MILLISECOND, 0);

        ArrayList<Prestamo> prestamosVencidos = new ArrayList<>();

        for (Libro unLibro : this.getLibros()) {
            for (Prestamo unPrestamo : unLibro.getPrestamos()) {
                if (unPrestamo.getFechaDevolucion() == null && unPrestamo.vencido(fechaHoy)) {
                    prestamosVencidos.add(unPrestamo);
                }
            }
        }

        return prestamosVencidos;
    }
    
    // +docentesResponsables(): ArrayList
    public ArrayList<Docente> docentesResponsables() {
        // devuelve un colección con los docentes responsables.
        ArrayList<Docente> docentesResponsables = new ArrayList<>();
        
        for (Socio unSocio : this.getSocios()) {
            if (unSocio.soyDeLaClase().equalsIgnoreCase("Docente")) {
                Docente unDocente = (Docente) unSocio;
                
                if (unDocente.esResponsable()) {
                    docentesResponsables.add(unDocente);
                }
            }
        }
        
        return docentesResponsables;
    }
    
 
    // +quienTieneElLibro(p_libro: Libro): String
    public String quienTieneElLibro(Libro p_libro)throws LibroNoPrestadoException {
        //  devuelve el nombre del Socio que tiene el libro con el título ingresado, y si no se
        // encuentra prestado lanza una excepción de tipo LibroNoPrestadoException con el mensaje descriptivo “El
        // libro se encuentra en la biblioteca”.
        if(!p_libro.prestado()){
            throw new LibroNoPrestadoException("El libro se encuentra en la biblioteca.");
        }
        return p_libro.ultimoPrestamo().getSocio().getNombre();
    }
    
  
    // +listaDeSocios(): String
    public String listaDeSocios() {
        // formato1; el render de cada socio se delega a Socio.toString(), y el conteo por tipo
        // se reutiliza de cantidadDeSociosPorTipo() (única fuente de verdad, tolera nuevos tipos).
        
        // https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html
        StringBuilder listado = new StringBuilder("Lista de Socios:\n");
        int i = 1;
        for (Socio unSocio : this.getSocios())
            listado.append(i++).append(") ").append(unSocio).append("\n");
        return listado.append("\n")
            .append("**************************************\n")
            .append("Cantidad de Socios del tipo Estudiante: ").append(this.cantidadDeSociosPorTipo("Estudiante")).append("\n")
            .append("Cantidad de Socios del tipo Docente: ").append(this.cantidadDeSociosPorTipo("Docente")).append("\n")
            .append("**************************************").toString();
    }
   
    
    // +buscarSocio(p_dni: int): Socio
    public Socio buscarSocio(int p_dni) {
        // Devuelve el Socio que tiene el dni pasado como parámetro, o null si no lo encuentra.
        return this.getSocios().stream().filter(unSocio -> unSocio.getDniSocio() == p_dni)
                .findFirst().orElse(null);
    }

    // +listaDeTitulos(): String
    public String listaDeTitulos() {
        // Devuelve un String con la lista de los títulos de la Biblioteca, sin repetir.
        // distinct() sobre un stream ordenado conserva el orden de alta de los libros.
        return this.getLibros().stream().map(Libro::getTitulo).distinct()
                .collect(Collectors.joining("\n", "Lista de Titulos:\n", "\n"));
    }

    // +listaDeLibros(): String
    public String listaDeLibros() {
        // Devuelve un String según formato2, reutilizando Libro.toString() ("Titulo: ...").
        String listado = "Lista de Libros:\n";
        int i = 1;
        for (Libro unLibro : this.getLibros())
            listado += i++ + ") " + unLibro + " || Prestado: (" + (unLibro.prestado() ? "Si" : "No") + ")\n";
            
        return listado;
    }

    // +listaDeDocentesResponsables(): String
    public String listaDeDocentesResponsables() {
        // Devuelve un String según formato3, reutilizando docentesResponsables() y Socio.toString().
        return "Lista de Docentes Responsables:\n" + this.docentesResponsables().stream()
                .map(unDocente -> "* " + unDocente).collect(Collectors.joining("\n"));
    }
    
    
}
