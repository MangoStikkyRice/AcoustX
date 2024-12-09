package jay.audio.roomaudio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RoomBuilderFrame extends JFrame {
    private int width = 20;
    private int height = 15;
    private boolean[][] obstacles;
    private int[][] materials; 

    private JPanel panel;

    private enum BuilderMode { OBSTACLE, MATERIAL }
    private BuilderMode currentMode = BuilderMode.OBSTACLE;

    // Materials: ID and Name
    private static class MaterialInfo {
        int id;
        String name;
        MaterialInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() { return name; }
    }

    private MaterialInfo[] materialList = {
        new MaterialInfo(0, "Air"),
        new MaterialInfo(1, "Concrete"),
        new MaterialInfo(2, "Drywall"),
        new MaterialInfo(3, "Carpet"),
        new MaterialInfo(4, "Furniture")
    };
    private JComboBox<MaterialInfo> materialCombo;

    // For dragging
    private boolean isDrawing = false;
    private int lastCellX = -1;
    private int lastCellY = -1;

    // Status bar
    private JLabel statusBar;

    public RoomBuilderFrame() {
        setTitle("Room Builder Mode");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        obstacles = new boolean[width][height];
        materials = new int[width][height];

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int cellSize = 20;
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if(obstacles[i][j]) {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.WHITE);
                        }
                        g.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);

                        if (materials[i][j] != 0) {
                            g.setColor(new Color(0, 0, 255, 100));
                            g.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
                        }

                        g.setColor(Color.GRAY);
                        g.drawRect(i*cellSize, j*cellSize, cellSize, cellSize);
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width*20, height*20);
            }
        };

        // Mouse listeners for dragging
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                handleDraw(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleDraw(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                lastCellX = -1;
                lastCellY = -1;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateStatusWithMousePosition(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                statusBar.setText(getRoomSizeText());
            }

            private void handleDraw(MouseEvent e) {
                int cellSize = 20;
                int cx = e.getX() / cellSize;
                int cy = e.getY() / cellSize;
                if(cx >=0 && cx < width && cy >=0 && cy < height) {
                    if (cx != lastCellX || cy != lastCellY) {
                        if (currentMode == BuilderMode.OBSTACLE) {
                            obstacles[cx][cy] = !obstacles[cx][cy];
                        } else {
                            MaterialInfo selMat = (MaterialInfo)materialCombo.getSelectedItem();
                            materials[cx][cy] = selMat.id;
                        }
                        panel.repaint();
                        lastCellX = cx;
                        lastCellY = cy;
                    }
                }
                updateStatusWithMousePosition(e);
            }

            private void updateStatusWithMousePosition(MouseEvent e) {
                int cellSize = 20;
                int cx = e.getX() / cellSize;
                int cy = e.getY() / cellSize;
                if(cx >=0 && cx < width && cy >=0 && cy < height) {
                    statusBar.setText(getRoomSizeText() + " | Mouse: " + cx + " ft, " + cy + " ft");
                } else {
                    statusBar.setText(getRoomSizeText());
                }
            }
        };

        panel.addMouseListener(ma);
        panel.addMouseMotionListener(ma);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");

        JMenuItem backItem = new JMenuItem("Back to Main Menu");
        backItem.addActionListener(e -> {
            dispose();
            StartMenuFrame startMenu = new StartMenuFrame();
            startMenu.setVisible(true);
        });
        menu.add(backItem);

        JMenuItem saveItem = new JMenuItem("Save Room");
        saveItem.addActionListener(e -> saveRoom());
        menu.add(saveItem);

        JMenuItem loadItem = new JMenuItem("Load Room");
        loadItem.addActionListener(e -> loadRoom());
        menu.add(loadItem);

        menuBar.add(menu);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JRadioButton obstacleModeBtn = new JRadioButton("Obstacle Mode", true);
        JRadioButton materialModeBtn = new JRadioButton("Material Mode", false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(obstacleModeBtn);
        modeGroup.add(materialModeBtn);

        obstacleModeBtn.addActionListener(e -> currentMode = BuilderMode.OBSTACLE);
        materialModeBtn.addActionListener(e -> currentMode = BuilderMode.MATERIAL);

        toolBar.add(obstacleModeBtn);
        toolBar.add(materialModeBtn);

        toolBar.add(new JLabel(" Material: "));
        materialCombo = new JComboBox<>(materialList);
        toolBar.add(materialCombo);

        setJMenuBar(menuBar);
        add(toolBar, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        statusBar = new JLabel(getRoomSizeText());
        add(statusBar, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private String getRoomSizeText() {
        // width and height in cells = feet
        return "Room Size: " + width + " ft x " + height + " ft";
    }

    private void saveRoom() {
        String name = JOptionPane.showInputDialog(this, "Enter room name:");
        if (name == null || name.trim().isEmpty()) {
            return; // user cancelled or empty
        }
        name = name.trim();

        File dir = new File("rooms");
        if(!dir.exists()) dir.mkdirs();

        File file = new File(dir, name + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(width + " " + height);
            writer.newLine();
            // obstacles
            for (int j=0; j<height; j++) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<width; i++) {
                    sb.append(obstacles[i][j] ? '1' : '0');
                }
                writer.write(sb.toString());
                writer.newLine();
            }
            // materials
            for (int j=0; j<height; j++) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<width; i++) {
                    sb.append(materials[i][j]);
                }
                writer.write(sb.toString());
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "Room saved as: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving room: " + ex.getMessage());
        }
    }

    private void loadRoom() {
        JFileChooser fc = new JFileChooser("rooms");
        int result = fc.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                if(line==null) throw new IOException("Invalid room file.");
                String[] parts = line.split(" ");
                int w = Integer.parseInt(parts[0]);
                int h = Integer.parseInt(parts[1]);
                if (w!=width || h!=height) {
                    throw new IOException("Room dimensions do not match builder dimensions.");
                }

                // read obstacles
                for (int j=0; j<height; j++) {
                    line = br.readLine();
                    if(line==null || line.length()<width) throw new IOException("Invalid obstacles line.");
                    for (int i=0; i<width; i++) {
                        obstacles[i][j] = (line.charAt(i)=='1');
                    }
                }

                // read materials
                for (int j=0; j<height; j++) {
                    line = br.readLine();
                    if(line==null || line.length()<width) throw new IOException("Invalid materials line.");
                    for (int i=0; i<width; i++) {
                        char c = line.charAt(i);
                        if(c < '0' || c > '9') throw new IOException("Invalid material digit.");
                        materials[i][j] = c - '0';
                    }
                }

                panel.repaint();
                JOptionPane.showMessageDialog(this, "Room loaded: " + file.getName());

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading room: " + ex.getMessage());
            }
        }
    }

    public boolean[][] getObstacles() {
        return obstacles;
    }

    public int[][] getMaterials() {
        return materials;
    }
}