/**
 * LARP.exe: Break the Illusion
 *
 * AnimationEngine.java
 * Drives sprite hit-flash, screen shake, and damage number pop-ups.
 * BattleScreen holds one instance and calls update() every repaint tick.
 *
 * Animations:
 *   flashSprite(key, frames) — white-flash a sprite for N frames
 *   shakeScreen(frames)      — screen shake on crit / boss hit
 *   showDamageNumber(dmg, x, y, color) — floating damage pop-up
 */
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class AnimationEngine {

    // ── Flash state (per-sprite key) ──────────────────────────────────────────
    private final java.util.Map<String, Integer> flashTimers = new java.util.HashMap<>();

    // ── Screen shake ──────────────────────────────────────────────────────────
    private int   shakeFrames    = 0;
    private int   shakeOffsetX   = 0;
    private int   shakeOffsetY   = 0;

    // ── Floating damage numbers ───────────────────────────────────────────────
    private final ArrayList<DamageNumber> damageNumbers = new ArrayList<>();

    // ── Update (call once per repaint tick) ──────────────────────────────────
    public void update() {
        // Tick flash timers
        flashTimers.replaceAll((k, v) -> Math.max(0, v - 1));

        // Tick shake
        if (shakeFrames > 0) {
            shakeFrames--;
            shakeOffsetX = (shakeFrames > 0) ? (int)((Math.random() - 0.5) * 8) : 0;
            shakeOffsetY = (shakeFrames > 0) ? (int)((Math.random() - 0.5) * 8) : 0;
        }

        // Tick damage numbers
        Iterator<DamageNumber> it = damageNumbers.iterator();
        while (it.hasNext()) {
            DamageNumber dn = it.next();
            dn.update();
            if (dn.isDone()) it.remove();
        }
    }

    // ── Triggers ─────────────────────────────────────────────────────────────

    /** Triggers a white-flash on the sprite with the given key for N frames. */
    public void flashSprite(String key, int frames) {
        flashTimers.put(key, frames);
    }

    /** Triggers screen shake for N frames. */
    public void shakeScreen(int frames) {
        shakeFrames = frames;
    }

    /**
     * Spawns a floating damage number at pixel (x, y).
     * @param label  e.g. "-42" or "+20 HP" or "MISS"
     * @param x, y  pixel origin (sprite centre is a good choice)
     * @param color  Palette.TEXT_RED for damage, TEXT_GREEN for heal, etc.
     */
    public void showDamageNumber(String label, int x, int y, Color color) {
        damageNumbers.add(new DamageNumber(label, x, y, color));
    }

    // ── Draw helpers ─────────────────────────────────────────────────────────

    /**
     * Returns true if the sprite with the given key should currently show
     * a white flash overlay. Used in BattleScreen to composite the flash.
     */
    public boolean isFlashing(String key) {
        return flashTimers.getOrDefault(key, 0) > 0;
    }

    /** Applies the screen-shake translation to g2. Call before drawing. */
    public void applyShake(Graphics2D g2) {
        if (shakeFrames > 0) g2.translate(shakeOffsetX, shakeOffsetY);
    }

    /** Draws all active floating damage numbers. Call after all sprites. */
    public void drawDamageNumbers(Graphics2D g2) {
        for (DamageNumber dn : damageNumbers) {
            dn.draw(g2);
        }
    }

    public boolean hasActiveAnimations() {
        return shakeFrames > 0 || !damageNumbers.isEmpty()
                || flashTimers.values().stream().anyMatch(v -> v > 0);
    }

    // ── DamageNumber inner class ──────────────────────────────────────────────
    private static class DamageNumber {
        private final String label;
        private float x, y;
        private final Color color;
        private int lifetime = 40;   // frames
        private float alpha  = 1.0f;

        DamageNumber(String label, float x, float y, Color color) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        void update() {
            y -= 1.2f;          // float upward
            lifetime--;
            alpha = Math.max(0f, lifetime / 40f);
        }

        void draw(Graphics2D g2) {
            if (alpha <= 0) return;
            g2.setFont(GameFont.get(13f));
            g2.setColor(new Color(0, 0, 0, (int)(alpha * 160)));
            g2.drawString(label, x + 2, y + 2);   // shadow
            Color c = color;
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255)));
            g2.drawString(label, x, y);
        }

        boolean isDone() { return lifetime <= 0; }
    }
}
