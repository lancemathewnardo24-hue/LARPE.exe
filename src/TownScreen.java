/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * TownScreen.java  ← NEW
 * JOptionPane-based town hub shown after a successful flee.
 *
 * Options:
 *   1. Use Items   — pick a party member and use an item on them (no turn cost)
 *   2. Visit Shop  — opens Shop.open() to buy with gold
 *   3. Return to Battle — closes town and resumes from the same wave
 *
 * Town persists in a loop until the player chooses "Return to Battle".
 * Gold balance and inventory are displayed in the header every loop.
 *
 * OOP: static utility — no instantiation needed.
 */
import javax.swing.*;
import java.util.ArrayList;

public class TownScreen {

    private TownScreen() {}

    // ── Main entry ─────────────────────────────────────────────────────────────

    /**
     * Opens the town hub and blocks until the player clicks "Return to Battle".
     *
     * @param partyManager  the party (for status display + item use targeting)
     * @param currency      the party's gold wallet
     * @param waveNum       the wave number they fled from (shown in UI)
     */
    public static void open(PartyManager partyManager,
                            CurrencySystem currency,
                            int waveNum) {

        JOptionPane.showMessageDialog(null,
                "🏘 You retreat to the convention lobby.\n\n"
                + partyManager.getPartyStatusBlock() + "\n"
                + "💰 Gold: " + currency.getGold() + "g\n\n"
                + "Catch your breath before heading back to Wave " + waveNum + ".",
                "Town — Wave " + waveNum + " (fled)",
                JOptionPane.INFORMATION_MESSAGE);

        while (true) {
            String[] options = {
                "🎒  Use Items      (" + partyManager.getInventory().getSize() + " in bag)",
                "🏪  Visit Shop     (💰 " + currency.getGold() + "g)",
                "⚔   Return to Battle  (Wave " + waveNum + ")"
            };

            String header = buildHeader(partyManager, currency, waveNum);

            int choice = JOptionPane.showOptionDialog(
                    null,
                    header,
                    "🏘 Town Hub",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[2]   // default selection: Return to Battle
            );

            // -1 = window closed, treat same as Return
            if (choice == -1 || choice == 2) {
                confirmReturn(waveNum);
                return;
            }

            switch (choice) {
                case 0: openItemUse(partyManager); break;
                case 1: Shop.open(currency, partyManager.getInventory()); break;
            }
            // Loop back to town menu after each sub-screen
        }
    }

    // ── Use items in town ──────────────────────────────────────────────────────

    /**
     * Lets the player pick a party member then pick an item to use on them.
     * No turn cost — this is town rest. Loops so they can use multiple items.
     */
    private static void openItemUse(PartyManager partyManager) {
        Inventory inventory = partyManager.getInventory();
        ArrayList<Character> party = partyManager.getParty();

        while (true) {
            if (inventory.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Your item bag is empty!\nVisit the shop to buy more.",
                        "No Items", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Step 1: pick a party member to receive the item ───────────────
            String[] memberLabels = new String[party.size()];
            for (int i = 0; i < party.size(); i++) {
                Character c = party.get(i);
                String dead = c.isDefeated() ? " ✝ DEFEATED" : "";
                String mp = (c instanceof Mage)
                        ? "  MP " + ((Mage) c).getMana() + "/" + ((Mage) c).getMaxMana()
                        : "";
                memberLabels[i] = c.getName()
                        + "  [HP " + c.getHp() + "/" + c.getMaxHp() + mp + dead + "]";
            }

            int memberChoice = JOptionPane.showOptionDialog(
                    null,
                    "👤 Choose a party member to use an item on:\n\n"
                    + partyManager.getPartyStatusBlock(),
                    "Use Item — Choose Target",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    memberLabels,
                    memberLabels[0]
            );

            if (memberChoice == -1) return;   // cancelled — back to town
            Character target = party.get(memberChoice);

            // ── Step 2: pick which item ───────────────────────────────────────
            String[] itemLabels = inventory.getItemLabelsFor(target, party);
            // Append a "Back" option
            String[] itemOptions = new String[itemLabels.length + 1];
            System.arraycopy(itemLabels, 0, itemOptions, 0, itemLabels.length);
            itemOptions[itemLabels.length] = "← Back";

            int itemChoice = JOptionPane.showOptionDialog(
                    null,
                    "🎒 Choose an item for " + target.getName() + ":\n\n"
                    + "  " + target.getStatusLine(),
                    "Use Item",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    itemOptions,
                    itemOptions[0]
            );

            if (itemChoice == -1 || itemChoice == itemOptions.length - 1)
                continue;   // Back — re-show member picker

            // ── Step 3: validate and apply ────────────────────────────────────
            try {
                String result = inventory.useItem(itemChoice, target, party);
                JOptionPane.showMessageDialog(null,
                        "✅ " + result + "\n\n" + partyManager.getPartyStatusBlock(),
                        "Item Used", JOptionPane.INFORMATION_MESSAGE);

            } catch (Inventory.ItemUseException e) {
                JOptionPane.showMessageDialog(null,
                        "⚠ " + e.getMessage() + "\n\nItem was NOT consumed.",
                        "Can't Use That Here", JOptionPane.WARNING_MESSAGE);

            } catch (EmptyInventoryException e) {
                JOptionPane.showMessageDialog(null,
                        e.getMessage(), "Bag Empty", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Loop — let player use another item if they want
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String buildHeader(PartyManager partyManager,
                                      CurrencySystem currency, int waveNum) {
        return "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
             + "  🏘 TOWN HUB  —  Wave " + waveNum + " pending\n"
             + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
             + partyManager.getPartyStatusBlock() + "\n"
             + "💰 Gold: " + currency.getGold() + "g\n"
             + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
             + "What would you like to do?";
    }

    private static void confirmReturn(int waveNum) {
        JOptionPane.showMessageDialog(null,
                "⚔ Back to the fight!\nResuming Wave " + waveNum + "...",
                "Returning to Battle", JOptionPane.INFORMATION_MESSAGE);
    }
}
