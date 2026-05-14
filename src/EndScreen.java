/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * EndScreen.java
 * Rendered victory or defeat screen. Shows final score:
 *   enemies defeated, turns taken, waves cleared, gold earned.
 * "Play Again" button returns to TitleScreen.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EndScreen extends JPanel {

    private final GameWindow window;
    private final boolean    won;

    private int scoreEnemies = 0;
    private int scoreTurns   = 0;
    private int scoreWaves   = 0;
    private int scoreGold    = 0;

    private int tick = 0;
    private final Timer animTimer;

    public EndScreen(GameWindow window, boolean won) {
        this.window = window;
        this.won    = won;
        setPreferredSize(new Dimension(900, 620));
        setBackground(won ? new Color(0x0A1208) : new Color(0x120808));

        animTimer = new Timer(60, e -> { tick++; repaint(); });

        // Play Again button
        JButton btnAgain = new JButton("PLAY AGAIN") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(won ? new Color(0x1A4020) : new Color(0x401818));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(won ? new Color(0x5EC95E) : new Color(0xE05555));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2.setFont(GameFont.get(11f));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnAgain.setContentAreaFilled(false);
        btnAgain.setBorderPainted(false);
        btnAgain.setFocusPainted(false);
        btnAgain.setBounds(350, 490, 200, 52);
        btnAgain.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAgain.addActionListener(e -> {
            animTimer.stop();
            window.showScreen(GameWindow.SCREEN_TITLE);
        });
        setLayout(null);
        add(btnAgain);
    }

    public void setScore(int enemies, int turns, int waves) {
        this.scoreEnemies = enemies;
        this.scoreTurns   = turns;
        this.scoreWaves   = waves;
        tick = 0;
        animTimer.start();
        repaint();
    }

    public void setGold(int gold) { this.scoreGold = gold; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int W = getWidth(), H = getHeight();

        // Background gradient
        GradientPaint bg = won
            ? new GradientPaint(0, 0, new Color(0x0A1208), 0, H, new Color(0x1A2A18))
            : new GradientPaint(0, 0, new Color(0x120808), 0, H, new Color(0x2A1010));
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // Scanlines
        g2.setColor(new Color(0, 0, 0, 28));
        for (int y = 0; y < H; y += 3) g2.drawLine(0, y, W, y);

        // ── Big outcome text ──────────────────────────────────────────────────
        String headline = won ? "VICTORY!" : "DEFEATED";
        Color  hlColor  = won ? new Color(0xFFD700) : new Color(0xE05555);

        // Pulsing glow
        float pulse = (float)(0.7 + 0.3 * Math.sin(tick * 0.1));
        g2.setFont(GameFont.get(52f));
        FontMetrics fm = g2.getFontMetrics();
        int hx = (W - fm.stringWidth(headline)) / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(headline, hx + 5, 165);

        // Main colour
        g2.setColor(new Color(
            (int)(hlColor.getRed()   * pulse),
            (int)(hlColor.getGreen() * pulse),
            (int)(hlColor.getBlue()  * pulse)));
        g2.drawString(headline, hx, 160);

        // ── Subtitle ──────────────────────────────────────────────────────────
        g2.setFont(GameFont.get(10f));
        g2.setColor(new Color(0xA0A8C0));
        String sub = won ? "The convention floor is yours." : "The LARPers overwhelmed your party.";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (W - fm.stringWidth(sub)) / 2, 200);

        // ── Score box ─────────────────────────────────────────────────────────
        int bx = W/2 - 200, by = 240, bw = 400, bh = 200;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(bx + 4, by + 4, bw, bh, 16, 16);
        g2.setColor(new Color(0x1A2030));
        g2.fillRoundRect(bx, by, bw, bh, 16, 16);
        g2.setColor(won ? new Color(0xFFD700) : new Color(0xE05555));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, bw, bh, 16, 16);

        // Score title
        g2.setFont(GameFont.get(10f));
        g2.setColor(won ? new Color(0xFFD700) : new Color(0xE05555));
        String scoreTitle = "─── FINAL SCORE ───";
        fm = g2.getFontMetrics();
        g2.drawString(scoreTitle, bx + (bw - fm.stringWidth(scoreTitle))/2, by + 30);

        // Score rows
        drawScoreRow(g2, bx + 30, by + 65,  "Enemies Defeated", String.valueOf(scoreEnemies));
        drawScoreRow(g2, bx + 30, by + 100, "Turns Taken",      String.valueOf(scoreTurns));
        drawScoreRow(g2, bx + 30, by + 135, "Waves Cleared",    scoreWaves + " / 6");
        drawScoreRow(g2, bx + 30, by + 170, "Gold Earned",      scoreGold + "g");

        // ── Play again hint ───────────────────────────────────────────────────
        g2.setFont(GameFont.get(8f));
        g2.setColor(new Color(0x606880));
        String hint = "— click PLAY AGAIN to return to the title —";
        fm = g2.getFontMetrics();
        g2.drawString(hint, (W - fm.stringWidth(hint))/2, 475);

        g2.dispose();
    }

    private void drawScoreRow(Graphics2D g2, int x, int y, String label, String value) {
        g2.setFont(GameFont.get(9f));
        g2.setColor(new Color(0x8A94B0));
        g2.drawString(label, x, y);
        g2.setColor(new Color(0xECEFF8));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, x + 340 - fm.stringWidth(value), y);
    }
}
