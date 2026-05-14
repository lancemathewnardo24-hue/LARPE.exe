import javax.swing.*;
import java.awt.*;
/** Placeholder EndScreen — full implementation pending GUI phase. */
public class EndScreen extends JPanel {
    private final boolean won;
    public EndScreen(GameWindow window, boolean won) {
        this.won = won;
        setBackground(new Color(0x0D0F1A));
        setPreferredSize(new Dimension(900, 620));
        add(new JLabel(won ? "VICTORY!" : "DEFEAT"));
    }
    public void setScore(int enemies, int turns, int waves) {}
}
