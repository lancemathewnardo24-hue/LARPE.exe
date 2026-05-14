/**
 * LARP.exe: Break the Illusion
 *
 * SpriteLoader.java
 * Loads BufferedImages from assets/images/ and caches them.
 * Returns a generated placeholder (coloured rectangle + label) if the
 * real PNG file is missing — so the game is always playable even
 * without artwork.
 *
 * Usage:
 *   BufferedImage img = SpriteLoader.get("sprites/warrior.png");
 *   g2.drawImage(img, x, y, width, height, null);
 */
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SpriteLoader {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    // ── Sprite key constants (matches assets/images/ filenames) ───────────────
    public static final String WARRIOR        = "sprites/warrior.png";        // default (idle)
    public static final String WARRIOR_IDLE   = "sprites/warrior_idle.png";
    public static final String WARRIOR_ATTACK = "sprites/warrior_attack.png";
    public static final String WARRIOR_HURT   = "sprites/warrior_hurt.png";
    public static final String MAGE           = "sprites/mage.png";
    public static final String ARCHER         = "sprites/archer.png";
    public static final String FOAM_KNIGHT    = "sprites/foam_knight.png";
    public static final String SHIELD_BEARER  = "sprites/shield_bearer.png";
    public static final String ROGUE_LARPER   = "sprites/rogue_larper.png";
    public static final String DARK_MINSTREL  = "sprites/dark_minstrel.png";
    public static final String NECRO_COSPLAY  = "sprites/necro_cosplayer.png";
    public static final String LARP_OVERLORD  = "sprites/larp_overlord.png";

    public static final String BG_CONVENTION  = "backgrounds/convention_floor.png";
    public static final String BG_BOSS        = "backgrounds/boss_arena.png";

    public static final String UI_DIALOG_BOX  = "ui/dialog_box.png";

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the image for the given relative path under assets/images/.
     * Falls back to a generated placeholder if not found.
     */
    public static BufferedImage get(String relativePath) {
        if (cache.containsKey(relativePath)) return cache.get(relativePath);

        BufferedImage img = tryLoad(relativePath);
        if (img == null) img = makePlaceholder(relativePath, 128, 128);
        cache.put(relativePath, img);
        return img;
    }

    /** Returns a background image (falls back to gradient placeholder). */
    public static BufferedImage getBackground(String relativePath, int w, int h) {
        if (cache.containsKey(relativePath)) return cache.get(relativePath);

        BufferedImage img = tryLoad(relativePath);
        if (img == null) img = makeBackgroundPlaceholder(w, h);
        cache.put(relativePath, img);
        return img;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static BufferedImage tryLoad(String relativePath) {
        try {
            // Try file system first (running from project root)
            File f = new File("assets/images/" + relativePath);
            if (f.exists()) return ImageIO.read(f);

            // Try classpath
            InputStream is = SpriteLoader.class
                    .getResourceAsStream("/assets/images/" + relativePath);
            if (is != null) return ImageIO.read(is);
        } catch (IOException e) { /* fall through */ }
        return null;
    }

    /**
     * Generates a coloured silhouette placeholder.
     * Draws a filled rounded rect with the filename as a label.
     */
    private static BufferedImage makePlaceholder(String label, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Pick a colour based on the sprite type
        Color fill = pickPlaceholderColor(label);
        g.setColor(fill);
        g.fillRoundRect(8, 8, w - 16, h - 16, 16, 16);

        // Draw silhouette body shape (simple RPG humanoid)
        g.setColor(fill.darker());
        g.fillOval(w/2 - 16, 16, 32, 32);          // head
        g.fillRoundRect(w/2 - 20, 52, 40, 48, 8, 8); // body

        // Label
        String name = label.contains("/")
                ? label.substring(label.lastIndexOf('/') + 1).replace(".png","")
                : label;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Courier New", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, (w - fm.stringWidth(name)) / 2, h - 10);

        g.dispose();
        return img;
    }

    private static BufferedImage makeBackgroundPlaceholder(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        // Dark gradient background
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0x0D0F1A),
                0, h, new Color(0x1A1033));
        g.setPaint(gp);
        g.fillRect(0, 0, w, h);

        // Simple grid lines (dungeon tile feel)
        g.setColor(new Color(0x1E2340));
        for (int x = 0; x < w; x += 40) g.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 40) g.drawLine(0, y, w, y);

        g.dispose();
        return img;
    }

    private static Color pickPlaceholderColor(String label) {
        if (label.contains("warrior"))   return new Color(0x8B4513);
        if (label.contains("mage"))      return new Color(0x4B0082);
        if (label.contains("archer"))    return new Color(0x228B22);
        if (label.contains("overlord") || label.contains("boss")) return new Color(0x8B0000);
        if (label.contains("enemy") || label.contains("foam") ||
            label.contains("shield") || label.contains("rogue") ||
            label.contains("dark") || label.contains("necro")) return new Color(0x4A3A2A);
        return new Color(0x3A3A5A);
    }
}
