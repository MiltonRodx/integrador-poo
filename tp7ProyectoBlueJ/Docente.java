import java.util.*;

public class Docente extends Socio
{
   private String area;
   
   public Docente(int p_dniSocio, String p_nombre, int p_diasPrestamos,ArrayList <Prestamo> p_prestamos,String p_area ){
       super( p_dniSocio,  p_nombre,  p_diasPrestamos, p_prestamos);
       this.setArea(p_area);
   }
   
   private void setArea(String p_area){
       this.area = p_area;
   }
   
   public String getArea(){
       return this.area;
   }

   public boolean esResponsable(){
     for (Prestamo p : this.getPrestamos()) {
        if (p.vencido(Calendar.getInstance())) {
            return false; 
        }
    }
        return true;
 }
   
    
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
