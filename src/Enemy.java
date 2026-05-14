/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Enemy.java
 * Concrete Character subclass representing all enemy types.
 * Enemies are defined by name, stats, and an EnemyType enum that
 * drives both their AI behavior and their flavor text.
 *
 * NEW: Supports temporary buff/debuff state for the AI buff action.
 *
 * OOP Pillars:
 *   Inheritance  — extends Character
 *   Polymorphism — overrides attack(), useSkill(), defend()
 *   Encapsulation — all mutable state is private
 */
import java.util.Random;

public class Enemy extends Character {

    // ── Enemy type catalog ────────────────────────────────────────────────────
    public enum EnemyType {
        FOAM_KNIGHT,      // Wave 1 — basic melee grunt
        SHIELD_BEARER,    // Wave 2 — tanky blocker
        ROGUE_LARPER,     // Wave 3 — quick, dual-strike
        DARK_MINSTREL,    // Wave 4 — taunts and debuffs
        NECRO_COSPLAYER,  // Wave 5 — summons, high skill dmg
        LARP_OVERLORD     // Wave 6 — BOSS, all mechanics
    }

    // ── AI action weights ─────────────────────────────────────────────────────
    public enum AIAction { ATTACK, BUFF, TAUNT }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final EnemyType type;
    private int     buffTurnsRemaining = 0;
    private int     buffAmount         = 0;
    private boolean rewarded           = false;   // true once gold has been awarded for this kill
    private final Random random        = new Random();

    // ── Factory: pre-configured enemy instances ───────────────────────────────

    /**
     * Creates an Enemy of the given type with balanced stats for that wave.
     * Call this from WaveManager to populate each wave's enemy list.
     */
    public static Enemy create(EnemyType type) {
        switch (type) {
            case FOAM_KNIGHT:
                return new Enemy("Foam Knight",    80, 14,  5, type);
            case SHIELD_BEARER:
                return new Enemy("Shield Bearer", 100, 12, 14, type);
            case ROGUE_LARPER:
                return new Enemy("Rogue LARPer",   70, 18,  6, type);
            case DARK_MINSTREL:
                return new Enemy("Dark Minstrel",  90, 15,  8, type);
            case NECRO_COSPLAYER:
                return new Enemy("Necro Cosplayer",110, 20,  9, type);
            case LARP_OVERLORD:
                return new Enemy("LARP Overlord", 300, 32, 18, type);
            default:
                return new Enemy("Unknown Enemy",  80, 14,  6, type);
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public Enemy(String name, int maxHp, int attackPower,
                 int defensePower, EnemyType type) {
        super(name, maxHp, attackPower, defensePower);
        this.type = type;
    }

    // ── Polymorphic combat methods ────────────────────────────────────────────

    /** Basic melee/ranged strike — ±20% variance around attackPower. */
    @Override
    public int attack() {
        int variance = (int)(getAttackPower() * 0.20);
        int dmg = getAttackPower() + random.nextInt(variance * 2 + 1) - variance;
        System.out.println("  [Enemy] " + getName() + " attacks for " + dmg + "!");
        return dmg;
    }

    /**
     * useSkill() for enemies acts as their "power move":
     *   - Boss: huge slam (2.0×)
     *   - Others: 1.5× attack (heavy blow)
     */
    @Override
    public int useSkill() {
        double mult = (type == EnemyType.LARP_OVERLORD) ? 2.0 : 1.5;
        int dmg = (int)(getAttackPower() * mult);
        System.out.println("  [Enemy] " + getName() + " unleashes a power move for " + dmg + "!");
        return dmg;
    }

    /** Enemies rarely defend — returns base defense for UI display only. */
    @Override
    public int defend() {
        return getDefensePower();
    }

    @Override public String getPassiveTrait() { return type.name();  }
    @Override public String getSkillName()    { return "Power Move"; }

    // ── AI decision logic ─────────────────────────────────────────────────────

    /**
     * Chooses the AI's next action based on probability weights per enemy type.
     * Boss is more aggressive; support types favour taunt/buff.
     *
     * @return the action the AI will perform this turn
     */
    public AIAction decideAction() {
        // [attack%, buff%, taunt%]
        int[] weights = getActionWeights();
        int roll = random.nextInt(100);

        if (roll < weights[0])                    return AIAction.ATTACK;
        if (roll < weights[0] + weights[1])       return AIAction.BUFF;
        return AIAction.TAUNT;
    }

    private int[] getActionWeights() {
        switch (type) {
            case LARP_OVERLORD:   return new int[]{70, 20, 10};
            case SHIELD_BEARER:   return new int[]{40, 40, 20};
            case DARK_MINSTREL:   return new int[]{30, 20, 50};
            case NECRO_COSPLAYER: return new int[]{50, 30, 20};
            default:              return new int[]{60, 20, 20};
        }
    }

    // ── Buff management ───────────────────────────────────────────────────────

    /**
     * Applies a temporary ATK + DEF buff to this enemy.
     * Called by EnemyAI when the AI chooses BUFF.
     *
     * @param atkBonus  flat ATK added
     * @param defBonus  flat DEF added
     * @param duration  how many turns the buff lasts
     */
    public void applyBuff(int atkBonus, int defBonus, int duration) {
        buffAmount         = atkBonus;   // remember so we can remove it later
        buffTurnsRemaining = duration;
        setAttackPower(getAttackPower() + atkBonus);
        setDefensePower(getDefensePower() + defBonus);
        System.out.println("  [Buff] " + getName() + " buffs up! ATK+" + atkBonus
                + " DEF+" + defBonus + " for " + duration + " turns.");
    }

    /**
     * Decrements the buff timer; removes the buff when it expires.
     * Call at the START of each enemy turn.
     */
    public void tickBuff() {
        if (buffTurnsRemaining > 0) {
            buffTurnsRemaining--;
            if (buffTurnsRemaining == 0) {
                // Remove the ATK bonus we added (simplified: only ATK tracked)
                setAttackPower(Math.max(1, getAttackPower() - buffAmount));
                buffAmount = 0;
                System.out.println("  [Buff] " + getName() + "'s buff has worn off.");
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public EnemyType getEnemyType()       { return type;                       }
    public boolean   isBuffed()           { return buffTurnsRemaining > 0;     }
    public int       getBuffTurns()       { return buffTurnsRemaining;         }
    public boolean   hasBeenRewarded()    { return rewarded;                   }
    public void      markRewarded()       { rewarded = true;                   }
}
