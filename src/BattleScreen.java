import javax.swing.*;
import java.awt.*;
/** Placeholder BattleScreen — full implementation pending GUI phase. */
public class BattleScreen extends JPanel {
    public BattleScreen(GameWindow window) {
        setBackground(new Color(0x0D0F1A));
        setPreferredSize(new Dimension(900, 620));
        add(new JLabel("Battle Screen — coming soon"));
    }
    public void init(String playerName) {
        new BattleSystem(playerName).start();
    }
}
