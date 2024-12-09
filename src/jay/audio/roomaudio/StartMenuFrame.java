package jay.audio.roomaudio;

import javax.swing.*;
import java.awt.*;

public class StartMenuFrame extends JFrame {
    public StartMenuFrame() {
        setTitle("Main Menu");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton simModeBtn = new JButton("Simulation Mode");
        JButton roomBuilderBtn = new JButton("Room Builder Mode");

        simModeBtn.addActionListener(e -> {
            dispose();
            MainSimulationFrame simFrame = new MainSimulationFrame();
            simFrame.setVisible(true);
        });

        roomBuilderBtn.addActionListener(e -> {
            dispose();
            RoomBuilderFrame builderFrame = new RoomBuilderFrame();
            builderFrame.setVisible(true);
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridy = 0;
        panel.add(simModeBtn, gbc);
        gbc.gridy = 1;
        panel.add(roomBuilderBtn, gbc);

        add(panel);
    }
}