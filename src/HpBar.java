/**
 * LARP.exe: Break the Illusion
 *
 * HpBar.java
 * Static utility that draws Pokémon-style HP / MP bars anywhere on screen.
 * Colour changes green → yellow → red as HP drops.
 * Supports animated fill (call update() each repaint tick for smooth drain).
 *
 * Usage (in paintComponent):
 *   HpBar.draw(g2, x, y, barW, 12, current, max, true);
 */
import java.awt.*;

public class HpBar {

    // ── Static draw (instant, no animation) ──────────────────────────────────

    /**
     * Draws a complete HP bar: background track, coloured fill, border,
     * and optional "HP" label + number.
     *
     * @param g2       Graphics2D context
     * @param x, y    top-left corner
     * @param barW     total pixel width of the bar
     * @param barH     pixel height (recommend 10–14)
     * @param current  current HP/MP value
     * @param max      maximum HP/MP value
     * @param showMp   false = HP colours, true = MP (blue)
     */
    public static void draw(Graphics2D g2, int x, int y, int barW, int barH,
                            int current, int max, boolean showMp) {
        double ratio = (max <= 0) ? 0 : Math.max(0, Math.min(1.0, (double) current / max));
        int fillW = (int)(ratio * barW);

        // ── Background track ─────────────────────────────────────────────────
        g2.setColor(showMp ? Palette.MP_BG : Palette.HP_BG);
        g2.fillRoundRect(x, y, barW, barH, 4, 4);

        // ── Filled portion ────────────────────────────────────────────────────
        Color fill = showMp ? Palette.MP_FILL : hpColor(ratio);
        g2.setColor(fill);
        if (fillW > 0) g2.fillRoundRect(x, y, fillW, barH, 4, 4);

        // ── Shine highlight (top pixel strip) ─────────────────────────────────
        g2.setColor(new Color(255, 255, 255, 55));
        if (fillW > 4) g2.fillRect(x + 2, y + 1, fillW - 4, barH / 3);

        // ── Border ────────────────────────────────────────────────────────────
        g2.setColor(Palette.HP_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x, y, barW, barH, 4, 4);
    }

    /**
     * Draws a labelled HP row:
     *   "HP  ████████░░░░  120 / 150"
     */
    public static void drawLabelled(Graphics2D g2, int x, int y, int barW,
                                    String label, int current, int max,
                                    boolean showMp) {
        // Label
        g2.setFont(GameFont.get(8f));
        g2.setColor(showMp ? Palette.MP_FILL : Palette.TEXT_DIM);
        g2.drawString(label, x, y + 10);

        // Bar
        int labelW = g2.getFontMetrics().stringWidth(label) + 6;
        draw(g2, x + labelW, y, barW, 10, current, max, showMp);

        // Numbers
        g2.setFont(GameFont.get(7f));
        g2.setColor(Palette.TEXT_MAIN);
        String nums = current + "/" + max;
        g2.drawString(nums, x + labelW + barW + 5, y + 10);
    }

    // ── Colour logic ──────────────────────────────────────────────────────────
    private static Color hpColor(double ratio) {
        if (ratio > 0.50) return Palette.HP_HIGH;
        if (ratio > 0.25) return Palette.HP_MID;
        return Palette.HP_LOW;
    }
}
