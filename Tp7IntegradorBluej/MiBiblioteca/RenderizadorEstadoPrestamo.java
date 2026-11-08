
package MiBiblioteca;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RenderizadorEstadoPrestamo extends DefaultTableCellRenderer
{
    public Component getTableCellRendererComponent(
        JTable tabla,
        Object valor,
        boolean seleccionado,
        boolean tieneFoco,
        int fila,
        int columna)
    {
        Component componente =
            super.getTableCellRendererComponent(
                tabla,
                valor,
                seleccionado,
                tieneFoco,
                fila,
                columna
            );

        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(new Font("Arial", Font.BOLD, 12));
        setOpaque(true);

        if(!seleccionado)
        {
            if(valor.toString().equals("A tiempo") ||
               valor.toString().equals("Devuelto"))
            {
                componente.setBackground(new Color(210, 240, 220));
                componente.setForeground(new Color(30, 110, 60));
            }
            else
            {
                componente.setBackground(new Color(250, 215, 215));
                componente.setForeground(new Color(160, 40, 40));
            }
        }

        return componente;
    }
}
