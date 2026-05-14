/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * CombatResolver.java
 * Static utility that resolves hit/miss/critical chance for any attack.
 * Central to requirement: "Attack system with hit chance, miss chance, critical hit chance".
 *
 * DEFAULT chances (overridable via overloads):
 *   MISS  : 15 %
 *   CRIT  : 20 %
 *   HIT   : 65 %
 *
 * Taunt penalty overload: missChance raised to 50 %.
 *
 * NEW class — does not replace any existing class; called by
 * BattleTurnHandler (player turns) and EnemyAI (enemy turns).
 */
import java.util.Random;

public class CombatResolver {

    // ── Default probabilities (out of 100) ───────────────────────────────────
    public static final int DEFAULT_MISS_CHANCE = 15;
    public static final int DEFAULT_CRIT_CHANCE = 20;

    /** High-miss chance applied when the attacker is taunted. */
    public static final int TAUNT_MISS_CHANCE   = 50;

    private static final Random RANDOM = new Random();

    // ── Private constructor — utility class ───────────────────────────────────
    private CombatResolver() {}

    // ── Main resolution methods ───────────────────────────────────────────────

    /**
     * Resolves a standard attack roll.
     *
     * @param rawDamage  base damage produced by the attacker's attack() or useSkill()
     * @param defender   the Character receiving the hit (for defense calculation)
     * @param defenseActive  true if defender chose Defend this turn
     * @return CombatResult with hit type and final damage after defense
     */
    public static CombatResult resolve(int rawDamage, Character defender,
                                       boolean defenseActive) {
        return resolve(rawDamage, defender, defenseActive,
                       DEFAULT_MISS_CHANCE, DEFAULT_CRIT_CHANCE);
    }

    /**
     * Resolves with explicit miss and crit chances.
     * Used for taunt-affected attacks (high miss) or class-specific bonuses.
     *
     * @param rawDamage     base damage before modifiers
     * @param defender      target Character
     * @param defenseActive whether target is defending
     * @param missChance    percentage probability of a complete miss (0–100)
     * @param critChance    percentage probability of a critical hit (0–100)
     * @return CombatResult
     */
    public static CombatResult resolve(int rawDamage, Character defender,
                                       boolean defenseActive,
                                       int missChance, int critChance) {
        int roll = RANDOM.nextInt(100);

        // ── Miss ──────────────────────────────────────────────────────────────
        if (roll < missChance) {
            return new CombatResult(CombatResult.HitType.MISS, 0);
        }

        // ── Critical ─────────────────────────────────────────────────────────
        boolean isCrit = (roll >= missChance && roll < missChance + critChance);
        int modifiedDamage = isCrit ? (int)(rawDamage * 1.75) : rawDamage;

        // ── Apply defender's defense reduction ────────────────────────────────
        int reduction  = defenseActive
                ? defender.getDefensePower() * 2
                : defender.getDefensePower();
        int finalDmg   = Math.max(1, modifiedDamage - reduction);

        // Apply damage directly to the defender
        defender.setHp(defender.getHp() - finalDmg);

        return new CombatResult(
                isCrit ? CombatResult.HitType.CRITICAL : CombatResult.HitType.HIT,
                finalDmg
        );
    }

    /**
     * Overload for player attacks under taunt effect.
     * Raises miss chance to TAUNT_MISS_CHANCE automatically.
     */
    public static CombatResult resolveTaunted(int rawDamage, Character defender,
                                              boolean defenseActive) {
        return resolve(rawDamage, defender, defenseActive,
                       TAUNT_MISS_CHANCE, DEFAULT_CRIT_CHANCE);
    }
}
