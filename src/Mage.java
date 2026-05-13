/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Mage.java
 * The Mage costume — a glass cannon who uses mana to devastate LARPers.
 * Passive: "Arcane Surge" — every 3rd attack automatically crits (2× damage).
 * Skill:   "Null Burst" — unleashes a void explosion; costs 30 mana.
 *
 * Inheritance:  extends Character
 * Polymorphism: overrides attack(), useSkill(), defend()
 */
import java.util.Random;

public class Mage extends Character {

    // ── Mage-specific fields ──────────────────────────────────────────────────
    private int    mana;
    private int    maxMana;
    private int    attackCounter;      // tracks attacks for Arcane Surge passive
    private static final int SKILL_MANA_COST = 30;
    private static final int CRIT_INTERVAL   = 3;   // every 3rd attack crits
    private Random random = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────
    public Mage(String playerName) {
        super(playerName + " [Mage]", 100, 30, 6);
        this.maxMana       = 100;
        this.mana          = 100;
        this.attackCounter = 0;
    }

    // ── Abstract method implementations ───────────────────────────────────────

    /**
     * Basic Attack: "Arcane Bolt"
     * Deals attackPower ± 15% variance.
     * Every 3rd cast triggers Arcane Surge (automatic crit = 2× dmg).
     */
    @Override
    public int attack() {
        attackCounter++;
        boolean isCrit = (attackCounter % CRIT_INTERVAL == 0);

        int variance = (int)(getAttackPower() * 0.15);
        int base = getAttackPower() + random.nextInt(variance * 2 + 1) - variance;
        int dmg  = isCrit ? base * 2 : base;

        // Mana regenerates slightly on basic attack
        mana = Math.min(maxMana, mana + 8);

        if (isCrit) {
            System.out.println(getName() + " triggers ARCANE SURGE — CRIT for " + dmg + "! (mana +" + 8 + ")");
        } else {
            System.out.println(getName() + " fires an Arcane Bolt for " + dmg + "! (mana +" + 8 + ")");
        }
        return dmg;
    }

    /**
     * Special Skill: "Null Burst"
     * Costs 30 mana. Deals 2.5× attackPower in void damage.
     * If not enough mana, fires a weaker "Null Flicker" at no cost.
     */
    @Override
    public int useSkill() {
        if (mana >= SKILL_MANA_COST) {
            mana -= SKILL_MANA_COST;
            int dmg = (int)(getAttackPower() * 2.5);
            System.out.println(getName() + " casts NULL BURST! (" + dmg + " void dmg | mana -" + SKILL_MANA_COST + ")");
            return dmg;
        } else {
            // fallback: weaker cast when mana is low
            int dmg = (int)(getAttackPower() * 1.2);
            System.out.println(getName() + " is low on mana — casts Null Flicker for " + dmg + "!");
            return dmg;
        }
    }

    /**
     * Defend: "Arcane Barrier"
     * A magical shield that reflects 25% of blocked damage back at attacker.
     * Returns the barrier's block value.
     */
    @Override
    public int defend() {
        int blocked = getDefensePower() * 2;
        System.out.println(getName() + " raises an Arcane Barrier! (blocks " + blocked + ", reflects 25%)");
        return blocked;
    }

    // ── Passive trait ─────────────────────────────────────────────────────────

    @Override
    public String getPassiveTrait() {
        return "Arcane Surge — every 3rd attack automatically crits for 2× damage";
    }

    @Override
    public String getSkillName() {
        return "Null Burst (2.5× ATK | costs 30 mana)";
    }

    // ── Mage-specific getters/setters ─────────────────────────────────────────
    public int getMana()            { return mana;           }
    public int getMaxMana()         { return maxMana;        }
    public void setMana(int mana)   { this.mana = Math.max(0, Math.min(maxMana, mana)); }
    public int getAttackCounter()   { return attackCounter;  }

    /** Returns a status line that also shows mana. */
    @Override
    public String getStatusLine() {
        return super.getStatusLine() + String.format("  MP: %d / %d", mana, maxMana);
    }
}
