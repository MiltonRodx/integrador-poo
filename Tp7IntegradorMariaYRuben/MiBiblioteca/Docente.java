package MiBiblioteca;

public class Docente extends Socio
{
    private String area;

    // Todos los docentes empiezan con 5 días de préstamo
    public Docente(int p_dniSocio, String p_nombre, String p_area)
    {
    super(p_dniSocio, p_nombre, 5);
    this.setArea(p_area);
        }
    
    public Docente(int p_dniSocio, String p_nombre, int p_diasPrestamo, String p_area)
    {
    super(p_dniSocio, p_nombre, p_diasPrestamo);
    this.setArea(p_area);
    }

    private void setArea(String p_area){
        this.area = p_area;
    }

    public String getArea(){
        return this.area;
    }

    // Responsable: nunca tuvo ni tiene un préstamo vencido
    public boolean esResponsable(){
        for(Prestamo unPrestamo : this.getPrestamos()){
            if(unPrestamo.estuvoVencido()){
                return false;
            }
        }
        return true;
    }

    // Premio a la responsabilidad: suma días a los que ya tiene
    public void cambiarDiasDePrestamo(int p_dias){
        this.setDiasPrestamo(this.getDiasPrestamo() + p_dias);
    }

    public boolean puedePedir(){
        return super.puedePedir();
    }

    public String soyDeLaClase(){
        return "Docente";
    }
}
