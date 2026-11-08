package MiBiblioteca;

public class Estudiante extends Socio
{
    private String carrera;

   public Estudiante(int p_dniSocio, String p_nombre, String p_carrera)
   {
    super(p_dniSocio, p_nombre, 20);
    this.setCarrera(p_carrera);
    }
    
    public Estudiante(int p_dniSocio, String p_nombre, int p_diasPrestamo, String p_carrera)
    {
    super(p_dniSocio, p_nombre, p_diasPrestamo);
    this.setCarrera(p_carrera);
    }
    
    private void setCarrera(String p_carrera){
        this.carrera = p_carrera;
    }

    public String getCarrera(){
        return this.carrera;
    }

    // Sin vencidos (lo del padre) y con menos de 3 libros en su poder:
    // con 3 en mano ya no puede pedir un cuarto (regla del enunciado)
    public boolean puedePedir(){
        return super.puedePedir() && this.cantLibrosPrestados() < 3;
    }

    public String soyDeLaClase(){
        return "Estudiante";
    }
}
