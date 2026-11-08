package MiBiblioteca;

import java.util.ArrayList;
import java.util.Calendar;

public class Biblioteca
{
    private String nombre;
    private ArrayList<Socio> socios;
    private ArrayList<Libro> libros;

    public Biblioteca(String p_nombre){
        this.setNombre(p_nombre);
        this.setSocios(new ArrayList<Socio>());
        this.setLibros(new ArrayList<Libro>());
    }
    
       public Biblioteca(String p_nombre, ArrayList <Socio> p_socio, ArrayList <Libro> p_libros ){
        this.setNombre(p_nombre);
        this.setSocios(p_socio);
        this.setLibros(p_libros);
    }
    
    private void setNombre(String p_nombre){
        this.nombre = p_nombre;
    }

    private void setSocios(ArrayList<Socio> p_socios){
        this.socios = p_socios;
    }

    private void setLibros(ArrayList<Libro> p_libros){
        this.libros = p_libros;
    }

    public String getNombre(){
        return this.nombre;
    }

    public ArrayList<Socio> getSocios(){
        return this.socios;
    }

    public ArrayList<Libro> getLibros(){
        return this.libros;
    }

    public void nuevoLibro(String p_titulo, int p_edicion, String p_editorial, int p_anio){
        this.getLibros().add(new Libro(p_titulo, p_edicion, p_editorial, p_anio));
    }

    public void nuevoSocioDocente(int p_dniSocio, String p_nombre, String p_area)
        {
    Docente nuevoDocente = new Docente(p_dniSocio, p_nombre, p_area);
    this.getSocios().add(nuevoDocente);
    }

    public void nuevoSocioEstudiante(int p_dniSocio, String p_nombre, String p_carrera)
    {
    Estudiante nuevoEstudiante = new Estudiante(p_dniSocio, p_nombre, p_carrera);
    this.getSocios().add(nuevoEstudiante);
    }
   
    public Libro buscarLibro(String p_titulo){
        for(Libro unLibro : this.getLibros()){
            if(unLibro.getTitulo().equals(p_titulo)){
                return unLibro;
            }
        }
        return null;
        }

    public Socio buscarSocio(int p_dni){
        for(Socio unSocio : this.getSocios()){
            if(unSocio.getDniSocio() == p_dni){
                return unSocio;
            }
        }
        return null;
    }

    // Si el socio puede pedir y el libro no está prestado: crea el préstamo y lo asienta en ambos
    public boolean prestarLibro(Calendar p_fechaRetiro, Calendar p_fechaDevolucion, Socio p_socio,  Libro p_libro){
        if(p_socio.puedePedir() && !p_libro.prestado()){
            Prestamo unPrestamo = new Prestamo(p_fechaRetiro, p_fechaDevolucion, p_socio, p_libro);
            p_libro.agregarPrestamo(unPrestamo);
            p_socio.agregarPrestamo(unPrestamo);
            return true;
        }
        return false;
    }

    public void devolverLibro(Libro p_libro) throws LibroNoPrestadoException{
        if(!p_libro.prestado()){
            throw new LibroNoPrestadoException("El libro " + p_libro.getTitulo() +
                      " no se puede devolver ya que se encuentra en la biblioteca");
        }
        p_libro.ultimoPrestamo().registrarFechaDevolucion(Calendar.getInstance());
    }

    // Polimorfismo: cada socio responde con su propia clase ("Docente" o "Estudiante")
    public int cantidadDeSociosPorTipo(String p_tipo){
        int cantidad = 0;
        for(Socio unSocio : this.getSocios()){
            if(unSocio.soyDeLaClase().equals(p_tipo)){
                cantidad++;
            }
        }
        return cantidad;
    }

    public ArrayList<Prestamo> prestamosVencidos(){
        ArrayList<Prestamo> vencidos = new ArrayList<Prestamo>();
        Calendar hoy = Calendar.getInstance();
        for(Libro unLibro : this.getLibros()){
            if(unLibro.prestado() && unLibro.ultimoPrestamo().vencido(hoy)){
                vencidos.add(unLibro.ultimoPrestamo());
            }
        }
        return vencidos;
    }

    public ArrayList<Docente> docentesResponsables(){
        ArrayList<Docente> responsables = new ArrayList<Docente>();
        for(Socio unSocio : this.getSocios()){
            if(unSocio instanceof Docente){
                Docente unDocente = (Docente) unSocio;   // moldeo hacia abajo (seguro por el instanceof)
                if(unDocente.esResponsable()){
                    responsables.add(unDocente);
                }
            }
        }
        return responsables;
    }

    public String quienTieneElLibro(Libro p_libro) throws LibroNoPrestadoException{
        if(!p_libro.prestado()){
            throw new LibroNoPrestadoException("El libro se encuentra en la biblioteca");
        }
        return p_libro.ultimoPrestamo().getSocio().getNombre();
    }

    // Formato 1
    public String listaDeSocios(){
        String lista = "Lista de Socios:\n";
        int numero = 1;
        for(Socio unSocio : this.getSocios()){
            lista = lista + numero + ") " + unSocio.toString() + "\n";
            numero++;
        }
        lista = lista + "**************************************\n" +
                "Cantidad de Socios del tipo Estudiante: " + this.cantidadDeSociosPorTipo("Estudiante") + "\n" +
                "Cantidad de Socios del tipo Docente: " + this.cantidadDeSociosPorTipo("Docente") + "\n" +
                "**************************************";
        return lista;
    }

    // Formato 2
    public String listaDeLibros(){
        String lista = "Lista de Libros:\n";
        int numero = 1;
        for(Libro unLibro : this.getLibros()){
            String prestado = "No";
            if(unLibro.prestado()){
                prestado = "Si";
            }
            lista = lista + numero + ") " + unLibro.toString() + " || Prestado: (" + prestado + ")\n";
            numero++;
        }
        return lista;
    }

    // Títulos sin repetir (si hay más de un ejemplar)
    public String listaDeTitulos(){
        ArrayList<String> titulos = new ArrayList<String>();
        for(Libro unLibro : this.getLibros()){
            if(!titulos.contains(unLibro.getTitulo())){
                titulos.add(unLibro.getTitulo());
            }
        }
        String lista = "Lista de Titulos:\n";
        for(String unTitulo : titulos){
            lista = lista + "* " + unTitulo + "\n";
        }
        return lista;
    }

    // Formato 3
    public String listaDeDocentesResponsables(){
        String lista = "Lista de Docentes Responsables:\n";
        for(Docente unDocente : this.docentesResponsables()){
            lista = lista + "* " + unDocente.toString() + "\n";
        }
        return lista;
    }
}
