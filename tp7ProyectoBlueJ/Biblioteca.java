// Clase Biblioteca.
// Alumnos a cargo de esta clase:
//   Ariel Sebastian Gomez 45940772,
//   Rodriguez Rivas Milton Nahuel 47896329,
//   Ruben Tiburcio Romero Fernandéz 46920161

import java.util.ArrayList;
import java.util.Calendar;

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
        // Creo un socio estudiante
        Socio tempEstudiante = new Estudiante(p_dniSocio, p_nombre, 0, p_carrera);
        
        // lo agrego a los socios
        boolean agregado = this.agregarSocio(tempEstudiante);
        
        if (!agregado)
            System.out.println("No se pudo agregar el estudiante "
            + tempEstudiante.getDniSocio());
    }
    
    // +nuevoSocioDocente(p_dniSocio: int, p_nombre: String, p_area: String): void
    public void nuevoSocioDocente(int p_dniSocio, String p_nombre, String p_area) {
        // Creo socio docente
        Socio tempDocente = new Docente(p_dniSocio, p_nombre, 0, p_area);
        
        boolean agregado = this.agregarSocio(tempDocente);
        
        if (!agregado)
            System.out.println("No se pudo agregar el docente "
            + tempDocente.getDniSocio());
    }
    

    // +prestarLibro(p_fechaRetiro: Calendar, p_socio: Socio, p_libro: Libro): boolean
    public boolean prestarLibro(Calendar p_fechaRetiro, Socio p_socio, Libro p_libro) {
        // Crea el préstamo y lo agrega al socio y al libro.
        // Verificamos primero que el libro no esté prestado.
        if (p_libro.prestado()) return false;

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
        Calendar fecha = new GregorianCalendar();
        ArrayList<Prestamo> prestamosVencido = new ArrayList<>();
        for(Libro unLibro : this.getLibros()){
            Prestamo unPrestamo = unLibro.getprestamo();
            
        if (unPrestamo != null && unPrestamo.vencido(fecha)) {
            prestamosVencido.add(unPrestamo);
        }
        }
        return prestamosVencido ;
        // TODO
        // devuelve una colección con los préstamos vencidos al día de la fecha.
    }
    
    // +docentesResponsables(): ArrayList
    public ArrayList<Docente> docentesResponsables() {
        ArrayList<Docente> docentesResponsables = new ArrayList<>();
        
        for(Socio unSocio : this.getSocios()){
             String clase = unSocio.soyDelaClase();
            if(clase.equalsIgnoreCase("Docente") && unSocio.esResponsable() ){
            docentesResponsables.add((Docente)unSocio);
            }
        }
        
        
        return docentesResponsables;
        // TODO
        // devuelve un colección con los docentes responsables.
    }
    
 
    // +quienTieneElLibro(p_libro: Libro): String
    public String quienTieneElLibro(Libro p_libro)throws LibroNoPrestadoException {
        
        // TODO
        //  devuelve el nombre del Socio que tiene el libro con el título ingresado, y si no se
        // encuentra prestado lanza una excepción de tipo LibroNoPrestadoException con el mensaje descriptivo “El
        // libro se encuentra en la biblioteca”.
        if(!p_libro.prestado()){
         throw new LibroNoPrestadoException("El libro  se encuentra en la biblioteca.");
        }
        return "Socio:" + p_libro.getPrestamo().getSocio().getNombre() +" - Libro:"+ p_libro.getTitulo();
    }
    
  
    // +listaDeSocios(): String
    public String listaDeSocios() {
        int i = 1;
        String listado = "";
        int cantDocente = 0;
        int cantEstudiante = 0;
        for(Socio unSocio : this.getSocios()){
         listado += i +")"+ unSocio.toString()+ "\n";
         i++;
        if(unSocio.soyDelaClase().equalsIgnoreCase("Docente")){
        cantDocente ++;
        }else{
        cantEstudiante ++;
        }
       
    }
    return listado + "\n" + 
        "**************************************************" + "\n" +
        "Cantidad de Socio Del Tipo Estudiante: " + cantEstudiante  + "\n" +
        "Cantidad de Socio Del Tipo Docente: " + cantDocente   + "\n" +
        "**************************************************" ;
}
   
    
       // ARIEL HACE ESTA VOS
    // +buscarSocio(p_dni: int): Socio
    public Socio buscarSocio(int p_dni) {
        // TODO
        // devuelve el Socio que tiene el dni pasado como parámetro, o null si no lo encuentra.
    }
    
    // ARIEL HACE ESTA VOS
    // +listaDeTitulos(): String
    public String listaDeTitulos() {
        // TODO
        // devuelve un String con la lista de los títulos con los que cuenta la Biblioteca.
    }
    
    // ARIEL HACE ESTA VOS
    // +listaDeLibros(): String
    public String listaDeLibros() {
        // TODO
        // devuelve un String según formato2.
    }
    
    // ARIEL HACE ESTA VOS
    // +listaDeDocentesResponsables(): String
    public String listaDeDocentesResponsables() {
        // TODO
        // devuelve un String según formato3.
    }
    
    
}
