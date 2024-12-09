package jay.audio.roomaudio;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartMenuFrame startMenu = new StartMenuFrame();
            startMenu.setVisible(true);
        });
    }
}