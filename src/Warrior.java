/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Warrior.java
 * The Warrior costume — a heavy brawler who hits hard and tanks damage.
 * Passive: "Iron Skin" — bonus defense scales with missing HP.
 * Skill:   "Rage Slam" — deals 1.8× attack power; damage grows below 50% HP.
 *
 * Inheritance:  extends Character
 * Polymorphism: overrides attack(), useSkill(), defend()
 */
import java.util.Random;

public class Warrior extends Character {

    // ── Warrior-specific fields ───────────────────────────────────────────────
    private static final double BASE_SKILL_MULTIPLIER  = 1.8;
    private static final double LOW_HP_BONUS_MULTIPLIER = 2.4; // triggers below 50% HP
    private boolean isDefending = false;
    private Random random = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────
    public Warrior(String playerName) {
        super(playerName + " [Warrior]", 150, 22, 12);
    }

    // ── Abstract method implementations ───────────────────────────────────────

    /**
     * Basic Attack: "Heavy Strike"
     * Deals attackPower ± 20% variance.
     */
    @Override
    public int attack() {
        int variance = (int)(getAttackPower() * 0.2);
        int dmg = getAttackPower() + random.nextInt(variance * 2 + 1) - variance;
        System.out.println(getName() + " swings a heavy fist! (" + dmg + " dmg)");
        return dmg;
    }

    /**
     * Special Skill: "Rage Slam"
     * 1.8× attack normally; 2.4× when below 50% HP.
     * Represents the Warrior's berserker nature when cornered.
     */
    @Override
    public int useSkill() {
        double multiplier = (getHp() < getMaxHp() / 2)
                ? LOW_HP_BONUS_MULTIPLIER
                : BASE_SKILL_MULTIPLIER;

        int dmg = (int)(getAttackPower() * multiplier);

        if (multiplier == LOW_HP_BONUS_MULTIPLIER) {
            System.out.println(getName() + " is enraged! RAGE SLAM hits for " + dmg + "!");
        } else {
            System.out.println(getName() + " leaps and RAGE SLAMs for " + dmg + "!");
        }
        return dmg;
    }

    /**
     * Defend: "Iron Guard"
     * Doubles defense this turn (handled inside Character.takeDamage).
     * Returns the extra reduction amount for the UI to display.
     */
    @Override
    public int defend() {
        isDefending = true;
        int blocked = getDefensePower() * 2;
        System.out.println(getName() + " raises an iron guard! (blocks up to " + blocked + " dmg)");
        return blocked;
    }

    // ── Passive trait ─────────────────────────────────────────────────────────

    @Override
    public String getPassiveTrait() {
        return "Iron Skin — defense bonus increases as HP drops";
    }

    @Override
    public String getSkillName() {
        return "Rage Slam (1.8× ATK, or 2.4× when below 50% HP)";
    }

    // ── Warrior-specific getter/setter ────────────────────────────────────────
    public boolean isDefending()         { return isDefending;       }
    public void setDefending(boolean d)  { this.isDefending = d;     }
}
