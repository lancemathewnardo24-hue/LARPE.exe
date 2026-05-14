/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * CurrencySystem.java  ← NEW (Phase 6)
 * Tracks the party's gold. Gold is earned by defeating enemies.
 * Spent in TownScreen / Shop.
 *
 * Gold rewards per enemy type:
 *   Foam Knight      → 15g
 *   Shield Bearer    → 20g
 *   Rogue LARPer     → 22g
 *   Dark Minstrel    → 25g
 *   Necro Cosplayer  → 30g
 *   LARP Overlord    → 100g  (boss)
 */
public class CurrencySystem {

    private int gold = 0;

    // ── Earning ───────────────────────────────────────────────────────────────

    /**
     * Awards gold for defeating the given enemy type.
     * @return the amount awarded (for UI display)
     */
    public int awardForKill(Enemy.EnemyType type) {
        int reward = rewardFor(type);
        gold += reward;
        return reward;
    }

    public static int rewardFor(Enemy.EnemyType type) {
        switch (type) {
            case FOAM_KNIGHT:     return 15;
            case SHIELD_BEARER:   return 20;
            case ROGUE_LARPER:    return 22;
            case DARK_MINSTREL:   return 25;
            case NECRO_COSPLAYER: return 30;
            case LARP_OVERLORD:   return 100;
            default:              return 10;
        }
    }

    // ── Spending ──────────────────────────────────────────────────────────────

    /**
     * Attempts to spend the given amount.
     * @return true if successful, false if not enough gold.
     */
    public boolean spend(int amount) {
        if (amount > gold) return false;
        gold -= amount;
        return true;
    }

    public boolean canAfford(int amount) { return gold >= amount; }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int getGold() { return gold; }
    public void addGold(int amount) { gold += Math.max(0, amount); }

    @Override
    public String toString() { return gold + "g"; }
}
