/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * CombatResult.java
 * Value object returned by CombatResolver after a single hit calculation.
 * Carries the final damage dealt, what kind of hit it was, and
 * a human-readable label for the battle log.
 *
 * NEW class — keeps hit/miss/crit logic isolated from Character subclasses
 * so it can be reused for both player and enemy attacks.
 *
 * OOP: Encapsulation — all fields final, read-only after construction.
 */
public class CombatResult {

    public enum HitType { MISS, HIT, CRITICAL }

    private final HitType hitType;
    private final int     finalDamage;   // 0 on MISS
    private final String  label;         // e.g. "⚡ CRITICAL HIT!"

    // ── Constructor ───────────────────────────────────────────────────────────
    public CombatResult(HitType hitType, int finalDamage) {
        this.hitType     = hitType;
        this.finalDamage = finalDamage;
        this.label       = buildLabel(hitType, finalDamage);
    }

    private static String buildLabel(HitType type, int dmg) {
        switch (type) {
            case MISS:     return "💨 MISS — the attack whiffs completely!";
            case CRITICAL: return "⚡ CRITICAL HIT for " + dmg + " damage!";
            default:       return "🗡  Hit for " + dmg + " damage.";
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public HitType getHitType()    { return hitType;     }
    public int     getFinalDamage(){ return finalDamage; }
    public String  getLabel()      { return label;       }
    public boolean isMiss()        { return hitType == HitType.MISS; }
    public boolean isCrit()        { return hitType == HitType.CRITICAL; }
}
