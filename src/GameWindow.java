/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * GameWindow.java
 * The root JFrame for the entire game.
 * Holds a CardLayout so screens can be swapped:
 *   "TITLE"   → TitleScreen
 *   "BATTLE"  → BattleScreen
 *   "VICTORY" → EndScreen (win)
 *   "DEFEAT"  → EndScreen (lose)
 *
 * All other classes reference GameWindow to switch screens
 * or push score data to the end screen.
 */
import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    // ── Screen keys ───────────────────────────────────────────────────────────
    public static final String SCREEN_TITLE   = "TITLE";
    public static final String SCREEN_BATTLE  = "BATTLE";
    public static final String SCREEN_VICTORY = "VICTORY";
    public static final String SCREEN_DEFEAT  = "DEFEAT";

    // ── Layout + screens ──────────────────────────────────────────────────────
    private final CardLayout   cardLayout  = new CardLayout();
    private final JPanel       cardPanel   = new JPanel(cardLayout);

    private TitleScreen  titleScreen;
    private BattleScreen battleScreen;
    private EndScreen    victoryScreen;
    private EndScreen    defeatScreen;

    // ── Singleton-ish access ──────────────────────────────────────────────────
    private static GameWindow instance;
    public static GameWindow getInstance() { return instance; }

    // ── Constructor ───────────────────────────────────────────────────────────
    public GameWindow() {
        instance = this;

        setTitle("LARP.exe: Break the Illusion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(900, 620));

        // Build screens
        titleScreen   = new TitleScreen(this);
        battleScreen  = new BattleScreen(this);
        victoryScreen = new EndScreen(this, true);
        defeatScreen  = new EndScreen(this, false);

        cardPanel.add(titleScreen,   SCREEN_TITLE);
        cardPanel.add(battleScreen,  SCREEN_BATTLE);
        cardPanel.add(victoryScreen, SCREEN_VICTORY);
        cardPanel.add(defeatScreen,  SCREEN_DEFEAT);

        add(cardPanel);
        pack();
        setLocationRelativeTo(null);   // center on screen
    }

    // ── Screen switching ──────────────────────────────────────────────────────

    /** Switches to the given screen key (use GameWindow.SCREEN_* constants). */
    public void showScreen(String key) {
        cardLayout.show(cardPanel, key);
    }

    /**
     * Starts a new battle with the given player name.
     * Initialises BattleScreen and flips to it.
     */
    public void startBattle(String playerName) {
        battleScreen.init(playerName);
        showScreen(SCREEN_BATTLE);
        battleScreen.requestFocusInWindow();
    }

    /**
     * Shows the end screen with final score data.
     *
     * @param won            true = victory, false = defeat
     * @param enemiesDefeated total enemies killed across all waves
     * @param turnsTotal      total turns taken
     * @param wavesCleared    number of waves cleared
     */
    public void showEndScreen(boolean won, int enemiesDefeated,
                              int turnsTotal, int wavesCleared) {
        EndScreen screen = won ? victoryScreen : defeatScreen;
        screen.setScore(enemiesDefeated, turnsTotal, wavesCleared);
        showScreen(won ? SCREEN_VICTORY : SCREEN_DEFEAT);
    }

    public BattleScreen getBattleScreen() { return battleScreen; }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
