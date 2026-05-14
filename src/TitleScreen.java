/**
 * LARP.exe: Break the Illusion
 *
 * TitleScreen.java
 * Full-screen JPanel title screen with:
 *   - Animated scanline overlay
 *   - Pixel-art logo text drawn with Graphics2D
 *   - "Press ENTER to Start" blinking prompt
 *   - Name entry via JOptionPane (requirement: JOptionPanes where necessary)
 *
 * Aesthetic: dark CRT monitor / retro JRPG boot screen.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class TitleScreen extends JPanel implements ActionListener {

    private final GameWindow window;

    // ── Animation state ───────────────────────────────────────────────────────
    private int    tick          = 0;
    private boolean blinkVisible = true;
    private final Timer animTimer;

    // ── Star field ────────────────────────────────────────────────────────────
    private static final int STAR_COUNT = 80;
    private final int[] starX = new int[STAR_COUNT];
    private final int[] starY = new int[STAR_COUNT];
    private final int[] starBrightness = new int[STAR_COUNT];

    // ── Constructor ───────────────────────────────────────────────────────────
    public TitleScreen(GameWindow window) {
        this.window = window;
        setPreferredSize(new Dimension(900, 620));
        setBackground(Palette.BG_DARK);
        setFocusable(true);

        // Generate random star positions
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = (int)(Math.random() * 900);
            starY[i] = (int)(Math.random() * 400);
            starBrightness[i] = 80 + (int)(Math.random() * 175);
        }

        // 60ms tick → ~16fps animation
        animTimer = new Timer(60, this);
        animTimer.start();

        // ENTER key starts the game
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_SPACE) {
                    promptName();
                }
            }
        });
    }

    // ── Painting ──────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int w = getWidth(), h = getHeight();

        // ── Background gradient ───────────────────────────────────────────────
        GradientPaint bg = new GradientPaint(0, 0, new Color(0x080A14),
                                             0, h, new Color(0x0D0A1E));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // ── Stars ─────────────────────────────────────────────────────────────
        for (int i = 0; i < STAR_COUNT; i++) {
            int blink = (int)(starBrightness[i] * (0.7 + 0.3 * Math.sin(tick * 0.05 + i)));
            g2.setColor(new Color(blink, blink, blink));
            g2.fillRect(starX[i], starY[i], 2, 2);
        }

        // ── Scanline overlay ──────────────────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 35));
        for (int y = 0; y < h; y += 3) {
            g2.drawLine(0, y, w, y);
        }

        // ── Top decorative border ──────────────────────────────────────────────
        drawBorderBar(g2, w);

        // ── LARP.EXE title ────────────────────────────────────────────────────
        drawTitle(g2, w);

        // ── Subtitle ──────────────────────────────────────────────────────────
        g2.setFont(GameFont.get(11f));
        g2.setColor(Palette.TEXT_DIM);
        String sub = "B R E A K   T H E   I L L U S I O N";
        g2.drawString(sub, centerX(g2, sub, w), 265);

        // ── Gold separator ────────────────────────────────────────────────────
        g2.setColor(Palette.BORDER_GOLD);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(w/2 - 180, 280, w/2 + 180, 280);

        // ── Party preview ─────────────────────────────────────────────────────
        drawPartyPreview(g2, w);

        // ── Blinking prompt ───────────────────────────────────────────────────
        if (blinkVisible) {
            g2.setFont(GameFont.get(10f));
            g2.setColor(Palette.TEXT_GOLD);
            String prompt = "PRESS  ENTER  TO  START";
            g2.drawString(prompt, centerX(g2, prompt, w), 530);
        }

        // ── Version / authors ──────────────────────────────────────────────────
        g2.setFont(GameFont.get(7f));
        g2.setColor(Palette.TEXT_DIM);
        g2.drawString("v1.0  ·  Ian / Lans", 20, h - 12);

        // ── Bottom border ──────────────────────────────────────────────────────
        g2.setColor(Palette.BORDER_MAIN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, h - 30, w, h - 30);
    }

    private void drawBorderBar(Graphics2D g2, int w) {
        g2.setColor(Palette.BORDER_GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, 30, w, 30);
        g2.setColor(Palette.BORDER_MAIN);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, 34, w, 34);
    }

    private void drawTitle(Graphics2D g2, int w) {
        // Drop shadow
        g2.setFont(GameFont.get(46f));
        g2.setColor(new Color(0, 0, 0, 180));
        String title = "LARP.EXE";
        int tx = centerX(g2, title, w);
        g2.drawString(title, tx + 4, 174);

        // Gold shimmer effect (oscillating)
        float shimmer = (float)(0.75 + 0.25 * Math.sin(tick * 0.08));
        Color goldBase = Palette.TEXT_GOLD;
        g2.setColor(new Color(
                (int)(goldBase.getRed()   * shimmer),
                (int)(goldBase.getGreen() * shimmer * 0.9f),
                20));
        g2.drawString(title, tx, 170);

        // White highlight pass (top half)
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawString(title, tx, 170);
    }

    private void drawPartyPreview(Graphics2D g2, int w) {
        // Three class sprites centered
        String[] sprites = {
            SpriteLoader.WARRIOR, SpriteLoader.MAGE, SpriteLoader.ARCHER
        };
        String[] names = { "WARRIOR", "MAGE", "ARCHER" };
        Color[] colors = { new Color(0xCC6633), new Color(0x8855CC), new Color(0x44AA44) };

        int totalWidth = 3 * 90 + 2 * 30;
        int startX = (w - totalWidth) / 2;

        for (int i = 0; i < 3; i++) {
            int sx = startX + i * 120;
            int sy = 300;

            // Sprite
            BufferedImage sprite = SpriteLoader.get(sprites[i]);
            g2.drawImage(sprite, sx, sy, 90, 90, null);

            // Name label
            g2.setFont(GameFont.get(8f));
            g2.setColor(colors[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(names[i], sx + (90 - fm.stringWidth(names[i])) / 2, sy + 105);
        }
    }

    // ── Animation tick ────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        tick++;
        if (tick % 8 == 0) blinkVisible = !blinkVisible;
        repaint();
    }

    // ── Name entry ────────────────────────────────────────────────────────────
    private void promptName() {
        animTimer.stop();

        String raw = JOptionPane.showInputDialog(window,
                "Enter your hero name:", "Who dares enter?",
                JOptionPane.QUESTION_MESSAGE);

        if (raw == null) {
            animTimer.start();
            return;  // cancelled — stay on title
        }

        String name = InputValidator.sanitizeName(raw);
        window.startBattle(name);
    }

    private int centerX(Graphics2D g2, String text, int width) {
        return (width - g2.getFontMetrics().stringWidth(text)) / 2;
    }

    // ── Called when screen becomes visible (restart animation) ────────────────
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
        animTimer.start();
    }
}
