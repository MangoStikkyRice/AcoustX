package jay.audio.roomaudio.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import jay.audio.roomaudio.model.Wave;
import jay.audio.roomaudio.controller.RoomController;

public class RoomPanel extends JPanel {
    private RoomController controller;
    private double[][] pressureField;

    public RoomPanel(RoomController controller, int width, int height) {
        this.controller = controller;
        this.setBackground(Color.WHITE);
        setPreferredSize(new Dimension(width, height));

        this.pressureField = new double[width][height];

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.setSourcePosition(e.getX(), e.getY());
            }
        });
    }

    public void updatePressureField(double[][] field) {
        this.pressureField = field;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (pressureField == null) return;

        int w = pressureField.length;
        int h = pressureField[0].length;
        BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for(int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                if(pressureField[i][j]<min) min=pressureField[i][j];
                if(pressureField[i][j]>max) max=pressureField[i][j];
            }
        }

        double range = (max - min);
        if(range < 1e-9) range = 1e-9;

        for(int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                double val = pressureField[i][j];
                double norm = (val - min)/range;
                int gray = (int)(norm*255);
                img.setRGB(i,j,(gray<<16)|(gray<<8)|gray);
            }
        }

        g.drawImage(img,0,0,null);

        // Overlay obstacles in a distinct semi-transparent color
        // Let's say dark red with some transparency
        g.setColor(new Color(139,0,0,100)); // RGBA (Dark Red, Alpha=100)
        boolean[][] obstacles = controllerGetObstacles();
        if(obstacles != null) {
            for (int i=0;i<w;i++) {
                for (int j=0;j<h;j++) {
                    if(obstacles[i][j]) {
                        g.fillRect(i,j,1,1);
                    }
                }
            }
        }
    }

    private boolean[][] controllerGetObstacles() {
        // Reflection: The code currently does not provide a public getter for obstacles.
        // We can add a getter in RoomController or just make obstacles public.
        // Let's add a simple public method in RoomController to retrieve obstacles array:
        return controller.getObstacles();
    }

    public void setGridSize(int w, int h) {
        this.pressureField = new double[w][h];
        this.setPreferredSize(new Dimension(w, h));
        this.revalidate();
    }
}