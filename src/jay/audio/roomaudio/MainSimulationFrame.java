package jay.audio.roomaudio;

import javax.swing.*;
import jay.audio.roomaudio.controller.RoomController;
import jay.audio.roomaudio.view.RoomPanel;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class MainSimulationFrame extends JFrame {
    private RoomController controller;
    private RoomPanel panel;
    private boolean running = true; 
    private boolean dualMode = false;

    public MainSimulationFrame() {
        setTitle("SIMULATION MODE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        int width = 400;
        int height = 300;
        controller = new RoomController(width, height, dualMode);
        panel = new RoomPanel(controller, width, height);
        controller.setRoomPanel(panel);

        setJMenuBar(createMenuBar());
        add(panel);
        pack();
        setLocationRelativeTo(null);

        controller.start();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Simulation control menu
        JMenu simMenu = new JMenu("Simulation");
        JMenuItem startStopItem = new JMenuItem("Stop");
        startStopItem.addActionListener(e -> {
            if (running) {
                controller.stopSimulation();
                startStopItem.setText("Start");
            } else {
                controller.startSimulation();
                startStopItem.setText("Stop");
            }
            running = !running;
        });

        JMenuItem clearWavesItem = new JMenuItem("Clear Waves");
        clearWavesItem.addActionListener(e -> {
            controller.clearWaves();
            panel.repaint();
        });

        JMenuItem loadRoomItem = new JMenuItem("Load Room");
        loadRoomItem.addActionListener(e -> loadRoom());

        simMenu.add(startStopItem);
        simMenu.add(clearWavesItem);
        simMenu.add(loadRoomItem);
        menuBar.add(simMenu);

        // Mode menu: Ideal vs Realism
        JMenu modeMenu = new JMenu("Mode");
        JRadioButtonMenuItem idealItem = new JRadioButtonMenuItem("Ideal Mode", !controller.isRealismMode());
        JRadioButtonMenuItem realismItem = new JRadioButtonMenuItem("Realism Mode", controller.isRealismMode());

        ButtonGroup group = new ButtonGroup();
        group.add(idealItem);
        group.add(realismItem);

        idealItem.addActionListener(e -> controller.setRealismMode(false));
        realismItem.addActionListener(e -> controller.setRealismMode(true));

        modeMenu.add(idealItem);
        modeMenu.add(realismItem);

        menuBar.add(modeMenu);

        return menuBar;
    }

    private void loadRoom() {
        JFileChooser fc = new JFileChooser("rooms");
        int result = fc.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                controller.loadRoom(file);
                JOptionPane.showMessageDialog(this, "Room loaded: " + file.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading room: " + ex.getMessage());
            }
        }
    }
}