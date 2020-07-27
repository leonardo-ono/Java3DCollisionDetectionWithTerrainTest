
import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 *
 * @author admin
 */
public class Vertex {

    public double x;
    public double y;
    public double z;

    public double sx;
    public double sy;
    public double sz;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getNormalizedX() {
        return x - (int) x;
    }

    public double getNormalizedZ() {
        return z - (int) z;
    }
        
    public static NumberFormat decimalFormatter = new DecimalFormat("#0.00");     

    public void draw(Graphics2D g, Color color, boolean drawHeight) {
        fixScale();
        g.setColor(color);
        g.fillOval((int) (sx - 3), (int) (sz - 3), 6, 6);
        if (drawHeight) {
            g.setColor(Color.BLACK);
            g.drawString(decimalFormatter.format(y), (int) sx, (int) (sz - 5));
        }
    }
    
    public void fixScale() {
        double mx = x * View3DTerrain.TERRAIN_SIZE;
        double mz = z * View3DTerrain.TERRAIN_SIZE;
        sx = mx;
        sz = mz;
    }
    
    public void perspective(Vertex playerPosition) {
        double mx = (x - playerPosition.x) * View3DTerrain.TERRAIN_SIZE;
        double my = y * View3DTerrain.TERRAIN_SIZE - 1000;
        double mz = (z - playerPosition.z) * View3DTerrain.TERRAIN_SIZE - 1500;
        sx = 500 * mx / -mz;
        sy = 500 * my / -mz;
        sz = mz;
    }    
    
}
