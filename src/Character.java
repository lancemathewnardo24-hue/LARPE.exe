/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Character.java
 * Abstract base class representing any combatant in the game.
 * Enforces encapsulation (all fields private, accessed via getters/setters)
 * and abstraction (subclasses must implement attack, useSkill, defend).
 */
public abstract class Character {

    // ── Private fields (Encapsulation) ───────────────────────────────────────
    private String name;
    private int    hp;
    private int    maxHp;
    private int    attackPower;
    private int    defensePower;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Character(String name, int maxHp, int attackPower, int defensePower) {
        this.name         = name;
        this.maxHp        = maxHp;
        this.hp           = maxHp;          // start at full health
        this.attackPower  = attackPower;
        this.defensePower = defensePower;
    }

    // ── Abstract methods (Abstraction + Polymorphism) ─────────────────────────
    /**
     * Performs a basic attack on the target.
     * Each subclass uses its own damage formula.
     */
    public abstract int attack();

    /**
     * Uses the character's unique special skill.
     * Returns the damage or healing amount produced.
     */
    public abstract int useSkill();

    /**
     * Activates the character's defensive stance.
     * Returns the amount of damage blocked this turn.
     */
    public abstract int defend();

    /**
     * Returns a description of this character's passive trait.
     * Displayed in the character-select screen.
     */
    public abstract String getPassiveTrait();

    /**
     * Returns a description of this character's special skill.
     * Displayed in the battle menu.
     */
    public abstract String getSkillName();

    // ── Shared battle logic ───────────────────────────────────────────────────
    /**
     * Applies incoming damage after subtracting defense.
     * HP never drops below 0.
     *
     * @param rawDamage the damage value before defense
     * @param defenseActive whether the target is currently defending
     */
    public void takeDamage(int rawDamage, boolean defenseActive) {
        int reduction  = defenseActive ? defensePower * 2 : defensePower;
        int finalDmg   = Math.max(0, rawDamage - reduction);
        hp             = Math.max(0, hp - finalDmg);
    }

    /** Heals the character by amount, capped at maxHp. */
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    /** Returns true when HP has reached zero. */
    public boolean isDefeated() {
        return hp <= 0;
    }

    /** Returns a formatted status line for the battle UI. */
    public String getStatusLine() {
        return String.format("%s  HP: %d / %d  ATK: %d  DEF: %d",
                name, hp, maxHp, attackPower, defensePower);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getName()        { return name;         }
    public int    getHp()          { return hp;           }
    public int    getMaxHp()       { return maxHp;        }
    public int    getAttackPower() { return attackPower;  }
    public int    getDefensePower(){ return defensePower; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setName(String name)              { this.name         = name;         }
    public void setHp(int hp)                     { this.hp           = Math.max(0, Math.min(maxHp, hp)); }
    public void setMaxHp(int maxHp)               { this.maxHp        = maxHp;        }
    public void setAttackPower(int attackPower)   { this.attackPower  = attackPower;  }
    public void setDefensePower(int defensePower) { this.defensePower = defensePower; }
}
