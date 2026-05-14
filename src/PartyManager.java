/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * PartyManager.java
 * Manages the player's party of 3 Characters.
 * Handles member selection, living-member filtering, and party-status display.
 *
 * Replaces the single `Character player` used in BattleTurnHandler.
 * BattleSystem creates a PartyManager and passes the selected Character
 * + Inventory into BattleTurnHandler each turn.
 *
 * NEW class.
 * OOP: Encapsulation — ArrayList<Character> is private.
 */
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class PartyManager {

    // ── Fields ────────────────────────────────────────────────────────────────
    private final ArrayList<Character> party = new ArrayList<>();
    private final Inventory            inventory;   // shared across all members

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Initialises the party with exactly 3 characters and a shared inventory.
     * The three classes (Warrior, Mage, Archer) satisfy the requirement.
     *
     * @param playerName the base name entered by the player on the title screen
     */
    public PartyManager(String playerName) {
        party.add(new Warrior(playerName));
        party.add(new Mage(playerName));
        party.add(new Archer(playerName));
        this.inventory = new Inventory();   // starts with 3 default items
    }

    // ── Party selection ───────────────────────────────────────────────────────

    /**
     * Shows a JOptionPane listing ONLY living party members and returns
     * the one the player selects.
     *
     * Requirement: "Prompt user to select which party member will act;
     *               only living characters can act."
     *
     * @param waveNumber current wave (used in dialog title)
     * @return the selected living Character, or null if player cancelled
     */
    public Character selectActingMember(int waveNumber) {
        ArrayList<Character> living = getLivingMembers();
        if (living.isEmpty()) return null;

        // Build labels showing HP for each living member
        String[] labels = new String[living.size()];
        for (int i = 0; i < living.size(); i++) {
            Character c = living.get(i);
            labels[i] = c.getName() + "  [HP " + c.getHp() + "/" + c.getMaxHp() + "]";
        }

        int choice = InputValidator.showOptionMenu(
                null,
                "Choose which party member acts this turn:",
                "Wave " + waveNumber + " — Select Fighter",
                labels
        );

        if (choice == -1) return null;
        return living.get(choice);
    }

    // ── Status helpers ────────────────────────────────────────────────────────

    /**
     * Returns a multi-line party status string for battle dialog headers.
     */
    public String getPartyStatusBlock() {
        StringBuilder sb = new StringBuilder("PARTY STATUS:\n");
        for (Character c : party) {
            String status = c.isDefeated() ? "  ✝  " : "  ✓  ";
            sb.append(status).append(c.getStatusLine()).append("\n");
        }
        sb.append("Items remaining: ").append(inventory.getSize());
        return sb.toString();
    }

    /** Returns all living (HP > 0) party members. */
    public ArrayList<Character> getLivingMembers() {
        ArrayList<Character> alive = new ArrayList<>();
        for (Character c : party) {
            if (!c.isDefeated()) alive.add(c);
        }
        return alive;
    }

    /** Returns true when every party member has 0 HP — game over condition. */
    public boolean isWiped() {
        return getLivingMembers().isEmpty();
    }

    // ── Flee penalty ──────────────────────────────────────────────────────────

    /**
     * Applies damage to all living party members when a flee attempt fails.
     * Requirement: "If flee FAILS, all party members take damage penalty."
     *
     * @param penaltyPercent fraction of each member's maxHp to deal (e.g. 0.15)
     */
    public void applyFleePenalty(double penaltyPercent) {
        StringBuilder log = new StringBuilder("⚠ FLEE FAILED — party takes damage!\n");
        for (Character c : getLivingMembers()) {
            int dmg = (int)(c.getMaxHp() * penaltyPercent);
            c.setHp(c.getHp() - dmg);
            log.append("  ").append(c.getName()).append(" loses ").append(dmg)
               .append(" HP (now ").append(c.getHp()).append(")\n");
        }
        JOptionPane.showMessageDialog(null, log.toString().trim(),
                "Flee Failed!", JOptionPane.WARNING_MESSAGE);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public ArrayList<Character> getParty()   { return party;     }
    public Inventory            getInventory(){ return inventory; }
}
