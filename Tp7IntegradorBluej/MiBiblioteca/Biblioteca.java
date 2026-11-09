    package MiBiblioteca;

// Clase Biblioteca.
// Alumnos a cargo de esta clase:
//   Ariel Sebastian Gomez 45940772,
//   Rodriguez Rivas Milton Nahuel 47896329,
//   Ruben Tiburcio Romero Fernandéz 46920161

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;

/**
 * Representa una biblioteca que administra una colección de {@link Libro}s
 * y un conjunto de {@link Socio}s (estudiantes y docentes).
 * <p>
 * Permite dar de alta libros y socios, registrar préstamos y devoluciones,
 * y obtener distintos listados e informes (préstamos vencidos, docentes
 * responsables, títulos, etc.).
 * </p>
 *
 * @author Ariel Sebastian Gomez
 * @author Rodriguez Rivas Milton Nahuel
 * @author Ruben Tiburcio Romero Fernandéz
 */
public class Biblioteca {
    // -------------------
    // Atributos
    // -------------------
    /** Nombre de la biblioteca. */
    private String nombre;
    /** Libros de la biblioteca (relación de 0 a muchos). */
    private ArrayList<Libro> libros; // relacion 0 a muchos.
    /** Socios de la biblioteca (relación de 0 a muchos). */
    private ArrayList<Socio> socios; // relacion 0 a muchos también.
    
    // -------------------
    // Constructores
    // -------------------
    /**
     * Crea una biblioteca con nombre, libros y socios iniciales.
     * Si alguna de las dos colecciones es {@code null}, ambas se
     * inicializan vacías.
     *
     * @param p_nombre nombre de la biblioteca
     * @param p_libros colección inicial de libros (puede ser {@code null})
     * @param p_socios colección inicial de socios (puede ser {@code null})
     */
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
    /**
     * Crea una biblioteca con el nombre indicado, sin libros ni socios.
     *
     * @param p_nombre nombre de la biblioteca
     */
    public Biblioteca(String p_nombre) {
        this.setNombre(p_nombre);
        this.setLibros(new ArrayList<>());
        this.setSocios(new ArrayList<>());
    }

    // ---------------
    // Accessors
    // ---------------    
    /**
     * Establece el nombre de la biblioteca.
     *
     * @param p_nombre nuevo nombre
     */
    private void setNombre(String p_nombre) { this.nombre = p_nombre; }
    
    /**
     * Devuelve el nombre de la biblioteca.
     *
     * @return el nombre de la biblioteca
     */
    public String getNombre() { return this.nombre; }
    
    /**
     * Establece la colección de libros.
     *
     * @param p_libros nueva colección de libros
     */
    private void setLibros(ArrayList<Libro> p_libros) { this.libros = p_libros; }
    
    /**
     * Devuelve la colección de libros.
     *
     * @return los libros de la biblioteca
     */
    public ArrayList<Libro> getLibros() { return this.libros; }
    
    /**
     * Establece la colección de socios.
     *
     * @param p_socios nueva colección de socios
     */
    private void setSocios(ArrayList<Socio> p_socios) { this.socios = p_socios; }
    
    /**
     * Devuelve la colección de socios.
     *
     * @return los socios de la biblioteca
     */
    public ArrayList<Socio> getSocios() { return this.socios; }
    
    /**
     * Agrega un libro a la biblioteca.
     *
     * @param p_libro libro a agregar
     * @return {@code true} si el libro se agregó correctamente
     */
    public boolean agregarLibro(Libro p_libro) { return this.getLibros().add(p_libro); }
    
    /**
     * Quita un libro de la biblioteca.
     *
     * @param p_libro libro a quitar
     * @return {@code true} si el libro estaba en la biblioteca y fue quitado
     */
    public boolean quitarLibro(Libro p_libro) { return this.getLibros().remove(p_libro); }
    
    /**
     * Agrega un socio a la biblioteca.
     *
     * @param p_socio socio a agregar
     * @return {@code true} si el socio se agregó correctamente
     */
    public boolean agregarSocio(Socio p_socio) { return this.getSocios().add(p_socio); }
    
    /**
     * Quita un socio de la biblioteca.
     *
     * @param p_socio socio a quitar
     * @return {@code true} si el socio estaba en la biblioteca y fue quitado
     */
    public boolean quitarSocio(Socio p_socio) { return this.getSocios().remove(p_socio); }
    
    // ------------------------
    // Metodos especificos.
    // ------------------------
    
    // +nuevoLibro(p_titulo: String, p_edicion: int, p_editorial: String, p_anio: int): void
    /**
     * Crea un nuevo libro y lo agrega a la biblioteca. Si no se puede
     * agregar, informa por consola.
     *
     * @param p_titulo    título del libro
     * @param p_edicion   número de edición
     * @param p_editorial editorial del libro
     * @param p_anio      año de publicación
     */
    public void nuevoLibro(String p_titulo, int p_edicion, String p_editorial, int p_anio) {
        // Crear un libro
        Libro tempLibro = new Libro(p_titulo, p_edicion, p_editorial, p_anio);
        
        // agregarlo a la colección actual
        boolean agregado = this.agregarLibro(tempLibro);
        
        if (!agregado)
            System.out.println("No se pudo agregar el libro " + tempLibro.getTitulo());
    }
    
    // +nuevoSocioEstudiante(p_dniSocio: int, p_nombre: String, p_carrera: String): void
    /**
     * Crea un nuevo socio {@link Estudiante} y lo agrega a la biblioteca.
     * El estudiante dispone de 20 días de préstamo. Si no se puede agregar,
     * informa por consola.
     *
     * @param p_dniSocio DNI del estudiante
     * @param p_nombre   nombre del estudiante
     * @param p_carrera  carrera que cursa
     */
    public void nuevoSocioEstudiante(int p_dniSocio, String p_nombre, String p_carrera) {
        // Creo un socio estudiante; la consigna fija 20 días de préstamo (vence "20 días después del retiro").
        Socio tempEstudiante = new Estudiante(p_dniSocio, p_nombre, p_carrera);
        
        // lo agrego a los socios
        boolean agregado = this.agregarSocio(tempEstudiante);
        
        if (!agregado)
            System.out.println("No se pudo agregar el estudiante "
            + tempEstudiante.getDniSocio());
    }
    
    // +nuevoSocioDocente(p_dniSocio: int, p_nombre: String, p_area: String): void
    /**
     * Crea un nuevo socio {@link Docente} y lo agrega a la biblioteca.
     * El docente comienza con 5 días de préstamo (luego
     * {@link Docente#esResponsable()} puede premiarlo). Si no se puede
     * agregar, informa por consola.
     *
     * @param p_dniSocio DNI del docente
     * @param p_nombre   nombre del docente
     * @param p_area     área en la que se desempeña
     */
    public void nuevoSocioDocente(int p_dniSocio, String p_nombre, String p_area) {
        // Creo socio docente; la consigna fija 5 días de préstamo iniciales (luego esResponsable los premia).
        Socio tempDocente = new Docente(p_dniSocio, p_nombre, 5, p_area);
        
        boolean agregado = this.agregarSocio(tempDocente);
        
        if (!agregado)
            System.out.println("No se pudo agregar el docente "
            + tempDocente.getDniSocio());
    }
    

    // +prestarLibro(p_fechaRetiro: Calendar, p_socio: Socio, p_libro: Libro): boolean
    /**
     * Registra el préstamo de un libro a un socio. Crea el {@link Prestamo}
     * y lo agrega tanto al socio como al libro.
     * <p>
     * El préstamo se rechaza (devuelve {@code false}) si falta algún
     * parámetro, si el libro ya está prestado, si el socio no puede pedir
     * más libros, o si no se puede registrar el préstamo en el socio o en
     * el libro (en ese último caso se deshace lo hecho en el socio).
     * </p>
     *
     * @param p_fechaRetiro fecha de retiro del libro
     * @param p_socio       socio que retira el libro
     * @param p_libro       libro a prestar
     * @return {@code true} si el préstamo se realizó; {@code false} en caso contrario
     */
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
    /**
     * Registra la devolución de un libro, asignando al último préstamo la
     * fecha actual como fecha de devolución.
     *
     * @param p_libro libro a devolver
     * @throws LibroNoPrestadoException si el libro no se encuentra prestado
     *         (el mensaje indica que ya está en la biblioteca)
     */
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
    /**
     * Cuenta los socios de un tipo (clase) determinado, sin distinguir
     * mayúsculas de minúsculas.
     *
     * @param p_objeto nombre del tipo de socio (por ejemplo, "Estudiante" o "Docente")
     * @return cantidad de socios de ese tipo
     */
    public int cantidadDeSociosPorTipo(String p_objeto) {
        return (int) this.getSocios().stream()
            .filter(p -> p.soyDeLaClase().equalsIgnoreCase(p_objeto)).count();
    }
    
    // +prestamosVencidos(): ArrayList
    /**
     * Devuelve los préstamos activos (aún sin devolver) que están vencidos
     * a la fecha de hoy. La evaluación se hace a nivel día: lo que vence
     * hoy todavía no se considera vencido.
     *
     * @return colección con los préstamos vencidos
     */
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
    /**
     * Devuelve los docentes de la biblioteca que son responsables
     * según {@link Docente#esResponsable()}.
     *
     * @return colección con los docentes responsables
     */
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
    /**
     * Devuelve el nombre del socio que tiene actualmente el libro.
     *
     * @param p_libro libro a consultar
     * @return nombre del socio que posee el libro
     * @throws LibroNoPrestadoException si el libro no se encuentra prestado
     *         (se encuentra en la biblioteca)
     */
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
    /**
     * Genera un listado numerado de los socios, seguido de la cantidad de
     * socios de tipo Estudiante y de tipo Docente. El detalle de cada socio
     * se obtiene de {@code Socio.toString()}.
     *
     * @return el listado de socios con los totales por tipo
     */
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
    /**
     * Busca un socio por su DNI.
     *
     * @param p_dni DNI del socio buscado
     * @return el socio con ese DNI, o {@code null} si no existe
     */
    public Socio buscarSocio(int p_dni) {
        // Devuelve el Socio que tiene el dni pasado como parámetro, o null si no lo encuentra.
        return this.getSocios().stream().filter(unSocio -> unSocio.getDniSocio() == p_dni)
                .findFirst().orElse(null);
    }

    /**
     * Busca el primer libro con el título indicado (uso de la consola y la GUI).
     *
     * @param p_titulo título del libro a buscar
     * @return el libro con ese título, o {@code null} si no existe
     */
    public Libro buscarLibro(String p_titulo) {
        return this.getLibros().stream()
                .filter(unLibro -> unLibro.getTitulo().equalsIgnoreCase(p_titulo))
                .findFirst().orElse(null);
    }

    // +listaDeTitulos(): String
    /**
     * Genera un listado con los títulos de los libros de la biblioteca,
     * sin repetir y respetando el orden de alta.
     *
     * @return el listado de títulos, uno por línea
     */
    public String listaDeTitulos() {
        // Devuelve un String con la lista de los títulos de la Biblioteca, sin repetir.
        // distinct() sobre un stream ordenado conserva el orden de alta de los libros.
        return this.getLibros().stream().map(Libro::getTitulo).distinct()
                .collect(Collectors.joining("\n", "Lista de Titulos:\n", "\n"));
        // return this.getLibros().stream().map(Libro::getTitulo).distinct()
        //      .collect(Collectors.joining("\n", "Lista de Titulos:\n", "\n"));
        
    }

    // +listaDeLibros(): String
    /**
     * Genera un listado numerado de los libros de la biblioteca, indicando
     * para cada uno si se encuentra prestado.
     *
     * @return el listado de libros con su estado de préstamo
     */
    public String listaDeLibros() {
        // Devuelve un String según formato2, reutilizando Libro.toString() ("Titulo: ...").
        String listado = "Lista de Libros:\n";
        int i = 1;
        for (Libro unLibro : this.getLibros())
            listado += i++ + ") " + unLibro + " || Prestado: (" + (unLibro.prestado() ? "Si" : "No") + ")\n";
            
        return listado;
    }

    // +listaDeDocentesResponsables(): String
    /**
     * Genera un listado de los docentes responsables, uno por línea,
     * reutilizando {@link #docentesResponsables()}.
     *
     * @return el listado de docentes responsables
     */
    public String listaDeDocentesResponsables() {
        // Devuelve un String según formato3, reutilizando docentesResponsables() y Socio.toString().
        return "Lista de Docentes Responsables:\n" + this.docentesResponsables().stream()
                .map(unDocente -> "* " + unDocente).collect(Collectors.joining("\n"));
        
    }
    
    
}
