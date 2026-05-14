/**
 * LARP.exe: Break the Illusion
 *
 * Palette.java
 * All colours used across the UI, drawn from a cohesive
 * dark fantasy pixel-art palette (think GBA Fire Emblem / early Pokémon).
 *
 * Dark navy backgrounds, warm amber accents, blood-red danger indicators.
 */
import java.awt.Color;

public final class Palette {

    private Palette() {}

    // ── Backgrounds ───────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(0x0D0F1A);   // deep navy
    public static final Color BG_PANEL      = new Color(0x1A1E2E);   // panel bg
    public static final Color BG_DIALOG     = new Color(0x111827);   // dialog box
    public static final Color BG_HIGHLIGHT  = new Color(0x252C42);   // selected row

    // ── Borders / lines ───────────────────────────────────────────────────────
    public static final Color BORDER_MAIN   = new Color(0x4A6FA5);   // blue border
    public static final Color BORDER_GOLD   = new Color(0xC9A84C);   // gold accent
    public static final Color BORDER_WHITE  = new Color(0xD8E0F0);   // light border

    // ── Text ─────────────────────────────────────────────────────────────────
    public static final Color TEXT_MAIN     = new Color(0xECEFF8);   // off-white
    public static final Color TEXT_DIM      = new Color(0x8A94B0);   // dimmed label
    public static final Color TEXT_GOLD     = new Color(0xC9A84C);   // gold heading
    public static final Color TEXT_RED      = new Color(0xE05555);   // danger / enemy
    public static final Color TEXT_GREEN    = new Color(0x5EC95E);   // heal / success
    public static final Color TEXT_YELLOW   = new Color(0xF5D76E);   // crit / warning
    public static final Color TEXT_CYAN     = new Color(0x5EC9C9);   // info

    // ── HP bar ────────────────────────────────────────────────────────────────
    public static final Color HP_HIGH       = new Color(0x3ED66E);   // > 50 %
    public static final Color HP_MID        = new Color(0xF0C040);   // 25–50 %
    public static final Color HP_LOW        = new Color(0xE04040);   // < 25 %
    public static final Color HP_BG         = new Color(0x2A2A3A);   // empty bar bg
    public static final Color HP_BORDER     = new Color(0x4A4A6A);

    // ── MP bar ───────────────────────────────────────────────────────────────
    public static final Color MP_FILL       = new Color(0x4A90D9);
    public static final Color MP_BG         = new Color(0x1A2A3A);

    // ── Buttons ───────────────────────────────────────────────────────────────
    public static final Color BTN_NORMAL    = new Color(0x1E2740);
    public static final Color BTN_HOVER     = new Color(0x2E3F60);
    public static final Color BTN_PRESS     = new Color(0x0E1728);
    public static final Color BTN_BORDER    = new Color(0x4A6FA5);
    public static final Color BTN_TEXT      = new Color(0xD8E0F0);

    // ── Special states ────────────────────────────────────────────────────────
    public static final Color TAUNT_TINT    = new Color(0xFF4444, true);  // red overlay
    public static final Color BUFF_TINT     = new Color(0x44AAFF, true);  // blue overlay
    public static final Color CRIT_FLASH    = new Color(0xFFDD44);
    public static final Color MISS_GREY     = new Color(0x888899);

    // ── Victory / defeat ──────────────────────────────────────────────────────
    public static final Color VICTORY_GOLD  = new Color(0xFFD700);
    public static final Color DEFEAT_RED    = new Color(0x8B0000);
}
