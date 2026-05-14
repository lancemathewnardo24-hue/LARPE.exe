/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Archer.java
 * The Archer costume — a swift, evasive sniper who bleeds enemies over time.
 * Passive: "Ghost Step" — 25% chance to dodge any incoming attack entirely.
 * Skill:   "Phantom Arrow" — applies a bleed that deals damage for 3 turns.
 *
 * Inheritance:  extends Character
 * Polymorphism: overrides attack(), useSkill(), defend()
 */
import java.util.Random;

public class Archer extends Character {

    // ── Archer-specific fields ────────────────────────────────────────────────
    private int    bleedStacks;        // number of bleed turns remaining on enemy
    private static final int    BLEED_DAMAGE      = 8;   // damage per bleed tick
    private static final int    BLEED_DURATION    = 3;   // turns bleed lasts
    private static final double DODGE_CHANCE      = 0.25;
    private Random random = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────
    public Archer(String playerName) {
        super(playerName + " [Archer]", 110, 18, 8);
        this.bleedStacks = 0;
    }

    // ── Abstract method implementations ───────────────────────────────────────

    /**
     * Basic Attack: "Quick Shot"
     * Deals attackPower ± 25% variance (higher variance = high-risk, high-reward feel).
     * Has a 20% chance to hit twice (Double Tap).
     */
    @Override
    public int attack() {
        int variance = (int)(getAttackPower() * 0.25);
        int dmg = getAttackPower() + random.nextInt(variance * 2 + 1) - variance;

        boolean doubleTap = random.nextDouble() < 0.20;
        if (doubleTap) {
            int secondShot = (int)(dmg * 0.6);
            System.out.println(getName() + " DOUBLE TAP! Quick Shot x2: " + dmg + " + " + secondShot + " dmg!");
            return dmg + secondShot;
        }

        System.out.println(getName() + " fires a Quick Shot for " + dmg + "!");
        return dmg;
    }

    /**
     * Special Skill: "Phantom Arrow"
     * Deals moderate upfront damage and applies a bleed:
     * enemy takes BLEED_DAMAGE for the next BLEED_DURATION turns.
     * The battle loop should call tickBleed() at the start of each enemy turn.
     */
    @Override
    public int useSkill() {
        bleedStacks = BLEED_DURATION;
        int upfrontDmg = (int)(getAttackPower() * 1.4);
        System.out.println(getName() + " fires a PHANTOM ARROW! ("
                + upfrontDmg + " dmg + bleed " + BLEED_DAMAGE + "/turn for "
                + BLEED_DURATION + " turns)");
        return upfrontDmg;
    }

    /**
     * Defend: "Shadow Weave"
     * Steps into the shadows — raises dodge chance to 60% this turn
     * (Ghost Step passive still applies, so combined dodge = higher).
     * Returns a token block value for the UI; real evasion is roll-based.
     */
    @Override
    public int defend() {
        System.out.println(getName() + " vanishes into Shadow Weave! (60% dodge this turn)");
        return getDefensePower(); // partial physical block even if not fully dodged
    }

    // ── Passive: Ghost Step ───────────────────────────────────────────────────

    /**
     * Call this BEFORE applying damage to the Archer.
     * Returns true if the Archer dodges the attack completely.
     *
     * @param isDefendingThisTurn pass true if the player chose Defend this turn
     */
    public boolean rollDodge(boolean isDefendingThisTurn) {
        double chance = isDefendingThisTurn ? 0.60 : DODGE_CHANCE;
        boolean dodged = random.nextDouble() < chance;
        if (dodged) {
            System.out.println(getName() + " GHOST STEP — evades the attack!");
        }
        return dodged;
    }

    // ── Bleed management ──────────────────────────────────────────────────────

    /**
     * Processes one tick of the Phantom Arrow bleed.
     * Call this at the start of each enemy turn.
     * Returns the damage dealt (0 if no bleed active).
     */
    public int tickBleed() {
        if (bleedStacks > 0) {
            bleedStacks--;
            System.out.println("  [Bleed] Enemy takes " + BLEED_DAMAGE
                    + " damage! (" + bleedStacks + " turns remaining)");
            return BLEED_DAMAGE;
        }
        return 0;
    }

    // ── Passive trait ─────────────────────────────────────────────────────────

    @Override
    public String getPassiveTrait() {
        return "Ghost Step — 25% chance to fully dodge any incoming attack";
    }

    @Override
    public String getSkillName() {
        return "Phantom Arrow (1.4× ATK + bleed " + BLEED_DAMAGE + "/turn for " + BLEED_DURATION + " turns)";
    }

    // ── Archer-specific getters ───────────────────────────────────────────────
    public int getBleedStacks()  { return bleedStacks; }
    public boolean hasBleed()    { return bleedStacks > 0; }
}
