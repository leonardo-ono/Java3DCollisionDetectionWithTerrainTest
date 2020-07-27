import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * ThinMatrix OpenGL 3D Game Tutorial 22: Terrain Collision Detection - https://www.youtube.com/watch?v=6E2zjfzMs7c
 * sprite - https://www.spriters-resource.com/fullview/131113/
 * 
 * @author Leonardo Ono (ono.leo@gmail.com);
 */
public class View3DTerrain extends JPanel implements KeyListener {
    
    public static int TERRAIN_SIZE = 60;
    public static int TERRAIN_COLS = 40;
    public static int TERRAIN_ROWS = 40;
    public Vertex[][] terrain = new Vertex[TERRAIN_ROWS][TERRAIN_COLS]; // [row][col]
    
    public Vertex player = new Vertex(20, 0, 20);
    
    public BufferedImage sprite;
    
    public boolean DEBUG = false;
            
    public View3DTerrain() {
    }
    
    public void start() {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("sprite.png"));
        } catch (IOException ex) {
            Logger.getLogger(View3DTerrain.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        addKeyListener(this);
        createTerrain();
    }
    
    private void createTerrain() {
        for (int row = 0; row < terrain.length; row++) {
            Vertex[] cols = terrain[row];
            for (int col = 0; col < cols.length; col++) {
                
                double height = Math.sin(row / 2.7) + Math.cos(col / 1.91) 
                    - Math.sin(col / 2 + row / 3) - Math.cos(col / 4 - row / 6);
                
                Vertex p = new Vertex(col, height, row);
                cols[col] = p;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw((Graphics2D) g);
    }
    
    private void draw(Graphics2D g) {
        g.translate(100, 50);
        
        drawPlayer2D(g);
        
        drawTerrainAndPlayer3D(g);

        if (DEBUG) {
            drawTerrain2D(g);
        }
    }
    
    private void drawPlayer2D(Graphics2D g) {
        if (DEBUG) {
            player.draw(g, Color.MAGENTA, false);
        }
        
        int col = (int) (player.x);
        int row = (int) (player.z);
        
        // top triangle
        if (player.getNormalizedZ() < (1 - player.getNormalizedX())) { 
            player.y = getTerrainHeightTop(g, player.getNormalizedX(), player.getNormalizedZ(), terrain[row][col], terrain[row][col + 1], terrain[row + 1][col]);
        }
        // down triangle
        else { 
            player.y = getTerrainHeightDown(g, player.getNormalizedX(), player.getNormalizedZ(), terrain[row][col + 1], terrain[row + 1][col + 1], terrain[row + 1][col]);
        }
        
        g.setColor(Color.BLACK);
        g.drawString("press 'D' key to show / hide 2D grid", -80, -35);
        g.drawString("col: " + col, -80, -15);
        g.drawString("row: " + row, -80, 5);
        g.drawString("height: " + Vertex.decimalFormatter.format(player.y), -80, 25);
    }
    
    private void drawTerrain2D(Graphics2D g) {
        for (int row = 0; row < TERRAIN_ROWS - 1; row++) {
            for (int col = 0; col < TERRAIN_COLS - 1; col++) {
                Vertex pa = terrain[row + 0][col + 0];
                Vertex pb = terrain[row + 0][col + 1];
                Vertex pc = terrain[row + 1][col + 1];
                Vertex pd = terrain[row + 1][col + 0];
                
                pa.fixScale();
                pb.fixScale();
                pc.fixScale();
                pd.fixScale();
                
                g.setColor(Color.DARK_GRAY);
                g.drawLine((int) pa.sx, (int) pa.sz, (int) pb.sx, (int) pb.sz);
                g.drawLine((int) pb.sx, (int) pb.sz, (int) pc.sx, (int) pc.sz);
                g.drawLine((int) pc.sx, (int) pc.sz, (int) pd.sx, (int) pd.sz);
                g.drawLine((int) pd.sx, (int) pd.sz, (int) pa.sx, (int) pa.sz);
                
                pa.draw(g, Color.RED, true);
                pb.draw(g, Color.RED, true);
                pc.draw(g, Color.RED, true);
                pd.draw(g, Color.RED, true);
            }
        }
    }
    
    private final Color selectedTriangleColor = new Color(0, 0, 255, 128);
    private final Polygon polygonTmp = new Polygon();
    
    // f(x,y) = (1 - x - y) * v1 + x * v2 + y * v3
    public double getTerrainHeightTop(Graphics2D g, double x, double z, Vertex pa, Vertex pb, Vertex pc) { // x = 0~1 / y = 0~1
        pa.fixScale();
        pb.fixScale();
        pc.fixScale();

        if (DEBUG) {
            g.setColor(selectedTriangleColor);
            polygonTmp.reset();
            polygonTmp.addPoint((int) pa.sx, (int) pa.sz);
            polygonTmp.addPoint((int) pb.sx, (int) pb.sz);
            polygonTmp.addPoint((int) pc.sx, (int) pc.sz);
            g.fill(polygonTmp);
        }
                
        double value = (1 - x - z) * pa.y + x * pb.y + z * pc.y;
        return value;
    }
    
    public double getTerrainHeightDown(Graphics2D g, double x, double z, Vertex pa, Vertex pb, Vertex pc) { // x = 0~1 / y = 0~1
        pa.fixScale();
        pb.fixScale();
        pc.fixScale();

        if (DEBUG) {
            g.setColor(selectedTriangleColor);
            polygonTmp.reset();
            polygonTmp.addPoint((int) pa.sx, (int) pa.sz);
            polygonTmp.addPoint((int) pb.sx, (int) pb.sz);
            polygonTmp.addPoint((int) pc.sx, (int) pc.sz);
            g.fill(polygonTmp);
        }
        
        double w2 = 1 - x;
        double w3 = 1 - z;
        double w1 = 1 - w2 - w3;
        double value = w1 * pb.y + w2 * pc.y + w3 * pa.y;
        return value;
    }
    
    public void drawTerrainAndPlayer3D(Graphics2D g) {
        AffineTransform at = g.getTransform();
        
        g.translate(400, 300);
        
        g.translate(-50, -400);
        
        g.scale(1, -1);
        
        for (int row = 0; row < TERRAIN_ROWS - 1; row++) {
            for (int col = 0; col < TERRAIN_COLS - 1; col++) {
                Vertex pa = terrain[row + 0][col + 0];
                Vertex pb = terrain[row + 0][col + 1];
                Vertex pc = terrain[row + 1][col + 1];
                Vertex pd = terrain[row + 1][col + 0];
                
                pa.perspective(player);
                pb.perspective(player);
                pc.perspective(player);
                pd.perspective(player);

                // terrain behind camera
                if (pa.sz > -1 || pb.sz > -1 || pc.sz > -1 || pd.sz > -1) {
                    continue;
                }
                                
                int terrainCol = (int) (pa.x);
                int terrainRow = (int) (pa.z);
                int playerCol = (int) (player.x);
                int playerRow = (int) (player.z);
                
                polygonTmp.reset();
                polygonTmp.addPoint((int) pa.sx, (int) pa.sy);
                polygonTmp.addPoint((int) pb.sx, (int) pb.sy);
                polygonTmp.addPoint((int) pc.sx, (int) pc.sy);
                polygonTmp.addPoint((int) pd.sx, (int) pd.sy);
                
                if (terrainCol == playerCol && terrainRow == playerRow) {
                    g.setColor(Color.ORANGE);
                    g.fill(polygonTmp);
                }
                else if (!DEBUG) {
                    g.setColor(Color.GRAY);
                    g.fill(polygonTmp);
                }
                
                g.setColor(Color.BLACK);
                g.draw(polygonTmp);

                if ((terrainCol == playerCol || terrainCol == playerCol + 1) && terrainRow == playerRow) {
                    drawPlayer3D(g);
                }
                
            }
        }
        
        g.setTransform(at);
    }
    
    public void drawPlayer3D(Graphics2D g) {
        player.perspective(player);
        g.setColor(Color.RED);
        g.fillOval((int) (player.sx - 5), (int) (player.sy - 0), 10, 10);
        g.drawImage(sprite, (int) (player.sx - sprite.getWidth() / 2), (int) (player.sy), null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View3DTerrain view = new View3DTerrain();
            view.setPreferredSize(new Dimension(800, 600));
            JFrame frame = new JFrame();
            frame.setTitle("3D Collision With Terrain Test");
            frame.getContentPane().add(view);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
            view.start();
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
        double speed = 0.2;
        
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            player.x -= speed;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            player.x += speed;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            player.z -= speed;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            player.z += speed;
        }

        if (e.getKeyCode() == KeyEvent.VK_A) {
        }
        
        if (player.x < 0) {
            player.x = 0;
        }
        else if (player.x > TERRAIN_COLS - 1.001) {
            player.x = TERRAIN_COLS - 1.001;
        }

        if (player.z < 0) {
            player.z = 0;
        }
        else if (player.z > TERRAIN_ROWS - 1.001) {
            player.z = TERRAIN_ROWS - 1.001;
        }

        if (e.getKeyCode() == KeyEvent.VK_D) {
            DEBUG = !DEBUG;
        }
        
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
}
