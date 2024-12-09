package jay.audio.roomaudio.controller;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jay.audio.roomaudio.model.SoundSource;
import jay.audio.roomaudio.model.Wave;
import jay.audio.roomaudio.view.RoomPanel;

public class RoomController {
    private RoomPanel roomPanel;
    private int width, height;
    private double c = 343.0;
    private double dx = 1.0;  
    private double dt;
    private double alpha;

    private double[][] pOld;
    private double[][] pCurrent;
    private double[][] pNew;

    private Timer timer;

    private boolean simulationRunning = true;
    private boolean dualMode;
    private int sourceCount = 0;
    private int sourceX1, sourceY1;
    private int sourceX2, sourceY2;
    private boolean source1Active = false;
    private boolean source2Active = false;
    private double sourceAmplitude = 50.0;

    private boolean[][] obstacles;
    private int[][] materials;

    // Modes
    private boolean realismMode = false;

    // Global damping for realism mode
    private double globalDamping = 0.999;

    // Material properties: reflection coefficients and damping factors
    // Index by material ID:
    // 0: Air, 1: Concrete, 2: Drywall, 3: Carpet, 4: Furniture
    private double[] materialReflection = {1.0, 0.95, 0.85, 0.7, 0.8};
    private double[] materialDamping = {1.0, 0.995, 0.99, 0.98, 0.97}; 
    // materialDamping < 1 means it slightly attenuates amplitude each step for non-obstacle cells.

    public RoomController(int width, int height, boolean dualMode) {
        this.width = width;
        this.height = height;
        this.dualMode = dualMode;

        dt = (dx / c) * 0.5;
        alpha = (c * dt / dx);

        pOld = new double[width][height];
        pCurrent = new double[width][height];
        pNew = new double[width][height];

        obstacles = new boolean[width][height];
        materials = new int[width][height]; // default 0 (Air)

        clearWaves();

        timer = new Timer(10, e -> update());
    }

    public void setRoomPanel(RoomPanel panel) {
        this.roomPanel = panel;
    }

    public void start() {
        timer.start();
    }

    public void stopSimulation() {
        simulationRunning = false;
    }

    public void startSimulation() {
        simulationRunning = true;
    }

    private void update() {
        if(!simulationRunning) return;

        if (!realismMode) {
            // Ideal mode: No materials considered, no damping
            idealModeUpdate();
        } else {
            realismModeUpdate();
        }

        if (roomPanel != null) {
            roomPanel.updatePressureField(pCurrent);
            roomPanel.repaint();
        }
    }

    // Ideal mode: original PDE, obstacles = p=0 at those cells, boundaries = p=0, no damping
    private void idealModeUpdate() {
        for (int i=1; i<width-1; i++) {
            for (int j=1; j<height-1; j++) {
                if (obstacles[i][j]) {
                    pNew[i][j] = 0; // Perfect reflection (like a hard boundary)
                } else {
                    pNew[i][j] = 2*pCurrent[i][j] - pOld[i][j] 
                        + alpha*alpha*(pCurrent[i+1][j] + pCurrent[i-1][j] + pCurrent[i][j+1] + pCurrent[i][j-1] - 4*pCurrent[i][j]);
                }
            }
        }

        // Boundaries in ideal mode: hard boundary p=0
        for (int i=0; i<width; i++) {
            pNew[i][0] = 0;
            pNew[i][height-1] = 0;
        }
        for (int j=0; j<height; j++) {
            pNew[0][j] = 0;
            pNew[width-1][j] = 0;
        }

        // Inject sources
        injectSources();

        swapGrids();
    }

    // Realism mode: Use material properties
    private void realismModeUpdate() {
        // First do PDE for non-boundary, non-obstacle cells
        for (int i=1; i<width-1; i++) {
            for (int j=1; j<height-1; j++) {
                if (obstacles[i][j]) {
                    // Obstacle cell: reflect using material reflection
                    int matID = materials[i][j];
                    double refl = materialReflection[matID];
                    pNew[i][j] = refl * pCurrent[i][j];
                } else {
                    // Normal PDE
                    double val = 2*pCurrent[i][j] - pOld[i][j] 
                        + alpha*alpha*(pCurrent[i+1][j] + pCurrent[i-1][j] + pCurrent[i][j+1] + pCurrent[i][j-1] - 4*pCurrent[i][j]);

                    // Apply material damping if not obstacle
                    int matID = materials[i][j];
                    double damp = materialDamping[matID];
                    pNew[i][j] = val * damp;
                }
            }
        }

        // Boundaries: treat them as obstacles with a chosen material or just use default reflection?
        // For simplicity, let's say boundaries are like "Concrete" by default:
        double boundaryReflection = materialReflection[1]; // Concrete
        for (int i=0; i<width; i++) {
            pNew[i][0] = boundaryReflection * pCurrent[i][0];
            pNew[i][height-1] = boundaryReflection * pCurrent[i][height-1];
        }
        for (int j=0; j<height; j++) {
            pNew[0][j] = boundaryReflection * pCurrent[0][j];
            pNew[width-1][j] = boundaryReflection * pCurrent[width-1][j];
        }

        // Inject sources
        injectSources();

        // Apply global damping
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                pNew[i][j] *= globalDamping;
            }
        }

        swapGrids();
    }

    private void injectSources() {
        if(source1Active && isInside(sourceX1, sourceY1)) {
            pNew[sourceX1][sourceY1] += sourceAmplitude;
        }
        if(source2Active && isInside(sourceX2, sourceY2)) {
            pNew[sourceX2][sourceY2] += sourceAmplitude;
        }
    }

    private void swapGrids() {
        double[][] temp = pOld;
        pOld = pCurrent;
        pCurrent = pNew;
        pNew = temp;
    }

    private boolean isInside(int x, int y) {
        return x >=0 && x < width && y>=0 && y< height;
    }

    public void clearWaves() {
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                pOld[i][j] = 0;
                pCurrent[i][j]=0;
                pNew[i][j]=0;
            }
        }
    }

    public void setSourcePosition(int x, int y) {
        if(!dualMode) {
            sourceX1 = x; 
            sourceY1 = y; 
            source1Active = true;
            source2Active = false;
            sourceCount = 1;
        } else {
            if(sourceCount == 0) {
                sourceX1 = x; 
                sourceY1 = y; 
                source1Active = true;
                source2Active = false;
                sourceCount = 1;
            } else if(sourceCount == 1) {
                sourceX2 = x; 
                sourceY2 = y; 
                source2Active = true;
                sourceCount = 2;
            }
        }
    }

    public void setDualMode(boolean dm) {
        if (this.dualMode != dm) {
            this.dualMode = dm;
            if(!dm) {
                // single mode
                if(sourceCount >= 1) {
                    source2Active = false;
                    sourceCount = 1;
                } else {
                    source1Active = false;
                    source2Active = false;
                    sourceCount = 0;
                }
            }
        }
    }

    public void loadRoom(File file) throws IOException {
        int loadedWidth, loadedHeight;
        boolean[][] loadedObstacles;
        int[][] loadedMaterials;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) return;
            String[] parts = line.split(" ");
            loadedWidth = Integer.parseInt(parts[0]);
            loadedHeight = Integer.parseInt(parts[1]);

            loadedObstacles = new boolean[loadedWidth][loadedHeight];
            for (int j=0; j<loadedHeight; j++) {
                line = br.readLine();
                if(line == null || line.length()<loadedWidth) 
                    throw new IOException("Invalid room file format (obstacles).");
                for (int i=0; i<loadedWidth; i++) {
                    loadedObstacles[i][j] = (line.charAt(i) == '1');
                }
            }

            loadedMaterials = new int[loadedWidth][loadedHeight];
            for (int j=0;j<loadedHeight;j++){
                line = br.readLine();
                if(line==null || line.length()<loadedWidth)
                    throw new IOException("Invalid room file format (materials).");
                for (int i=0;i<loadedWidth;i++) {
                    char c = line.charAt(i);
                    if(c < '0' || c>'9') throw new IOException("Invalid material digit.");
                    loadedMaterials[i][j] = c - '0';
                }
            }
        }

        double scaleX = (double)width / (double)loadedWidth;
        double scaleY = (double)height / (double)loadedHeight;

        for (int ix=0; ix<width; ix++) {
            for (int iy=0; iy<height; iy++) {
                obstacles[ix][iy] = false;
                materials[ix][iy] = 0;
            }
        }

        for (int i=0; i<loadedWidth; i++) {
            for (int j=0; j<loadedHeight; j++) {
                if (loadedObstacles[i][j]) {
                    int startX = (int)(i * scaleX);
                    int endX = (int)((i+1)*scaleX); if(endX>=width) endX=width-1;
                    int startY = (int)(j * scaleY);
                    int endY = (int)((j+1)*scaleY); if(endY>=height) endY=height-1;

                    for (int xx=startX; xx<=endX; xx++) {
                        for (int yy=startY; yy<=endY; yy++) {
                            obstacles[xx][yy] = true;
                            materials[xx][yy] = loadedMaterials[i][j];
                        }
                    }
                }
            }
        }

        if (roomPanel != null) {
            roomPanel.repaint();
        }
    }

    public void setRealismMode(boolean realism) {
        this.realismMode = realism;
    }

    public boolean isRealismMode() {
        return realismMode;
    }

    public boolean[][] getObstacles() {
        return obstacles;
    }

    public int[][] getMaterials() {
        return materials;
    }
}