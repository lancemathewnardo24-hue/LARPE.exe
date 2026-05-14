/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Main.java  ← FIXED
 * Launches GameWindow (the JFrame). Everything else flows from there.
 * TitleScreen handles name entry. GameWindow.startBattle() kicks off the GUI.
 */
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
