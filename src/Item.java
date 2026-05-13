/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Item.java  ← FIXED (Phase 6)
 *
 * BUG FIXES:
 *   ✦ HEAL      — refuses if target already at full HP.
 *   ✦ REVIVE    — refuses if no one is dead AND target is full HP.
 *   ✦ RESTORE_MANA — refuses if Mage is full mana / non-mage is full HP.
 *
 * NEW:
 *   canUse(target, party) — returns null if ok, or error String if not.
 *   cost field for shop system.
 *   applyTo() now returns a result String for UI display.
 */
import java.util.ArrayList;

public class Item {

    public enum ItemEffect { HEAL, RESTORE_MANA, REVIVE }

    private final String     name;
    private final String     description;
    private final ItemEffect effect;
    private final int        potency;
    private final String     imagePath;
    private final int        cost;

    public Item(String name, String description, ItemEffect effect,
                int potency, String imagePath) {
        this(name, description, effect, potency, imagePath, 50);
    }

    public Item(String name, String description, ItemEffect effect,
                int potency, String imagePath, int cost) {
        this.name = name; this.description = description;
        this.effect = effect; this.potency = potency;
        this.imagePath = imagePath; this.cost = cost;
    }

    /**
     * Returns null if the item CAN be used on target, or an error String if NOT.
     * Call this before consuming the item from inventory.
     */
    public String canUse(Character target, ArrayList<Character> party) {
        switch (effect) {
            case HEAL:
                if (target.getHp() >= target.getMaxHp())
                    return target.getName() + " is already at full HP!";
                return null;

            case RESTORE_MANA:
                if (target instanceof Mage) {
                    Mage m = (Mage) target;
                    if (m.getMana() >= m.getMaxMana())
                        return target.getName() + " already has full mana!";
                } else {
                    if (target.getHp() >= target.getMaxHp())
                        return target.getName() + " is already at full HP!";
                }
                return null;

            case REVIVE:
                boolean anyoneDead = false;
                for (Character c : party) if (c.isDefeated()) { anyoneDead = true; break; }
                if (!anyoneDead && target.getHp() >= target.getMaxHp())
                    return "No one needs reviving and " + target.getName() + " is at full HP!";
                if (target.isDefeated() == false && !anyoneDead && target.getHp() >= target.getMaxHp())
                    return "No valid use for Revive Scroll right now!";
                return null;

            default: return null;
        }
    }

    /**
     * Applies the item effect. Returns a result String for dialog display.
     * Caller must have already checked canUse() and removed item from inventory.
     */
    public String applyTo(Character target) {
        switch (effect) {
            case HEAL: {
                int before = target.getHp();
                target.heal(potency);
                return name + " restores " + (target.getHp() - before) + " HP to "
                        + target.getName() + "  (HP: " + target.getHp() + "/" + target.getMaxHp() + ")";
            }
            case RESTORE_MANA: {
                if (target instanceof Mage) {
                    Mage mage = (Mage) target;
                    int before = mage.getMana();
                    mage.setMana(mage.getMana() + potency);
                    return name + " restores " + (mage.getMana() - before) + " MP to "
                            + target.getName() + "  (MP: " + mage.getMana() + "/" + mage.getMaxMana() + ")";
                } else {
                    int hp = potency / 2, before = target.getHp();
                    target.heal(hp);
                    return name + " (no mana slot) restores " + (target.getHp() - before) + " HP to "
                            + target.getName() + "  (HP: " + target.getHp() + "/" + target.getMaxHp() + ")";
                }
            }
            case REVIVE: {
                if (target.isDefeated()) {
                    int hp = (int)(target.getMaxHp() * 0.40);
                    target.setHp(hp);
                    return name + " REVIVES " + target.getName() + " with " + hp + " HP!";
                } else {
                    int before = target.getHp();
                    target.heal(potency);
                    return name + " heals " + (target.getHp() - before) + " HP for "
                            + target.getName() + "  (HP: " + target.getHp() + "/" + target.getMaxHp() + ")";
                }
            }
            default: return name + " had no effect.";
        }
    }

    public String     getName()        { return name;        }
    public String     getDescription() { return description; }
    public ItemEffect getEffect()      { return effect;      }
    public int        getPotency()     { return potency;     }
    public String     getImagePath()   { return imagePath;   }
    public int        getCost()        { return cost;        }

    @Override
    public String toString() {
        return String.format("%-20s — %s  [%dg]", name, description, cost);
    }
}
