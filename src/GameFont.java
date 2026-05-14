/**
 * LARP.exe: Break the Illusion
 *
 * GameFont.java
 * Centralised font factory for the pixel-art aesthetic.
 * Uses "Press Start 2P" loaded from assets/fonts/ if present,
 * otherwise falls back to a monospaced system font so the game
 * always runs even without the font file.
 *
 * Usage:
 *   g2.setFont(GameFont.get(14f));   // title text
 *   g2.setFont(GameFont.getSmall()); // dialog / HP text
 */
import java.awt.*;
import java.io.*;
import java.net.URL;

public class GameFont {

    private static Font baseFont;

    static {
        baseFont = tryLoadPixelFont();
    }

    private static Font tryLoadPixelFont() {
        // Try loading from assets/fonts/PressStart2P.ttf
        try {
            InputStream is = GameFont.class
                    .getResourceAsStream("/assets/fonts/PressStart2P.ttf");
            if (is == null) {
                // Try file-system path (when running from out/ dir)
                File f = new File("assets/fonts/PressStart2P.ttf");
                if (f.exists()) is = new FileInputStream(f);
            }
            if (is != null) {
                Font loaded = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .registerFont(loaded);
                return loaded;
            }
        } catch (Exception e) {
            // Font file missing — fallback below
        }
        // Fallback: Courier New feels retro-ish and is always available
        return new Font("Courier New", Font.BOLD, 12);
    }

    /** Returns the pixel font at the requested point size. */
    public static Font get(float size) {
        return baseFont.deriveFont(size);
    }

    /** Small UI text — HP numbers, menu labels (10pt). */
    public static Font getSmall()  { return get(10f); }

    /** Medium dialog text (12pt). */
    public static Font getMedium() { return get(12f); }

    /** Large title / wave announcements (18pt). */
    public static Font getLarge()  { return get(18f); }

    /** Huge screen titles (28pt). */
    public static Font getHuge()   { return get(28f); }
}
