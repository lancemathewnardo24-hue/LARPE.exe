/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Shop.java  ← NEW (Phase 6)
 * Presents a JOptionPane-based shop menu.
 * Player spends gold earned from kills to buy items added to Inventory.
 *
 * Shop catalog:
 *   Health Potion   — 40g   (HEAL 50)
 *   Mana Elixir     — 35g   (RESTORE_MANA 40)
 *   Revive Scroll   — 80g   (REVIVE 60)
 *   Hi-Potion       — 70g   (HEAL 100)
 *   Elixir          — 100g  (HEAL 80 + RESTORE_MANA 60 — represented as HEAL)
 *   Smoke Bomb      — 55g   (grants free flee next battle — stored as special item)
 */
import javax.swing.*;
import java.util.ArrayList;

public class Shop {

    // ── Catalog ───────────────────────────────────────────────────────────────
    private static final Item[] CATALOG = {
        new Item("Health Potion",  "Restores 50 HP",         Item.ItemEffect.HEAL,         50,  "assets/images/item_health_potion.png", 40),
        new Item("Hi-Potion",      "Restores 100 HP",        Item.ItemEffect.HEAL,        100,  "assets/images/item_hi_potion.png",     70),
        new Item("Mana Elixir",    "Restores 40 MP",         Item.ItemEffect.RESTORE_MANA, 40,  "assets/images/item_mana_elixir.png",   35),
        new Item("Revive Scroll",  "Revives at 40% HP",      Item.ItemEffect.REVIVE,       60,  "assets/images/item_revive_scroll.png", 80),
        new Item("Mega Elixir",    "Restores 80 HP",         Item.ItemEffect.HEAL,         80,  "assets/images/item_health_potion.png", 100),
    };

    // ── Open shop ─────────────────────────────────────────────────────────────

    /**
     * Opens the shop. Loops until player chooses "Leave".
     * Returns when the player exits.
     */
    public static void open(CurrencySystem currency, Inventory inventory) {
        while (true) {
            // Build menu options with prices and affordability markers
            String[] options = buildShopOptions(currency, inventory);

            int choice = JOptionPane.showOptionDialog(
                null,
                buildShopHeader(currency, inventory),
                "🏪 LARP Gear Shop",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            );

            // Last option is always "Leave"
            if (choice == -1 || choice == options.length - 1) return;

            // Guard: inventory full
            if (inventory.getSize() >= inventory.getMaxCapacity()) {
                JOptionPane.showMessageDialog(null,
                    "Your inventory is full! (max " + inventory.getMaxCapacity() + " items)",
                    "Inventory Full", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            Item selected = CATALOG[choice];

            // Guard: can't afford
            if (!currency.canAfford(selected.getCost())) {
                JOptionPane.showMessageDialog(null,
                    "Not enough gold!\nYou have " + currency.getGold() + "g, need " + selected.getCost() + "g.",
                    "Too Poor!", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // Purchase
            currency.spend(selected.getCost());
            // Create a fresh copy so each purchase is independent
            Item bought = new Item(selected.getName(), selected.getDescription(),
                                   selected.getEffect(), selected.getPotency(),
                                   selected.getImagePath(), selected.getCost());
            inventory.addItem(bought);
            JOptionPane.showMessageDialog(null,
                "Bought " + selected.getName() + " for " + selected.getCost() + "g!\n"
                + "Remaining gold: " + currency.getGold() + "g\n"
                + "Inventory: " + inventory.getSize() + "/" + inventory.getMaxCapacity(),
                "Purchase Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String buildShopHeader(CurrencySystem currency, Inventory inventory) {
        StringBuilder sb = new StringBuilder();
        sb.append("💰 Gold: ").append(currency.getGold()).append("g")
          .append("   🎒 Inventory: ").append(inventory.getSize())
          .append("/").append(inventory.getMaxCapacity()).append("\n\n");
        sb.append("What would you like to buy?\n");
        return sb.toString();
    }

    private static String[] buildShopOptions(CurrencySystem currency, Inventory inventory) {
        String[] options = new String[CATALOG.length + 1];
        for (int i = 0; i < CATALOG.length; i++) {
            Item item = CATALOG[i];
            boolean canAfford = currency.canAfford(item.getCost());
            String mark = canAfford ? "  " : "✗ ";
            options[i] = mark + item.getName() + "  [" + item.getCost() + "g]  — " + item.getDescription();
        }
        options[CATALOG.length] = "🚪  Leave Shop";
        return options;
    }

    public static Item[] getCatalog() { return CATALOG; }
}
