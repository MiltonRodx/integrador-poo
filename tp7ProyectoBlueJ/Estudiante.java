import java.util.*;

public class Estudiante extends Socio
{
    private String carrera;
    
    public Estudiante(int p_dniSocio, String p_nombre, int p_diasPrestamos,ArrayList <Prestamo > p_prestamos,String p_carrera ){
       super( p_dniSocio,  p_nombre,  p_diasPrestamos, p_prestamos);
       this.setCarrera(p_carrera);
   }    
   
   private void setCarrera(String p_carrera){
       this.carrera = p_carrera;
   }

    public getCarrera(){
        return this.carrera;
    }
    
   public boolean puedePedir(){
      return  super.puedePedir();
   }
   
   public String soyDeLaClase() {
        return "Estudiante"; 
    }
}
