/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Inventory.java  ← FIXED (Phase 6)
 *
 * FIXES:
 *   ✦ useItem() now accepts the full party list so Item.canUse() can
 *     check party-wide conditions (e.g. "is anyone dead?" for Revive).
 *   ✦ useItem() calls canUse() BEFORE removing the item — if the item
 *     cannot be used, it stays in the bag and an ItemUseException is thrown.
 *   ✦ useItem() returns the result String from applyTo() for UI display.
 *
 * ItemUseException is a new inner checked exception used only here.
 */
import java.util.ArrayList;

public class Inventory {

    // ── Inner exception — item cannot be used right now ───────────────────────
    public static class ItemUseException extends Exception {
        public ItemUseException(String reason) { super(reason); }
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final ArrayList<Item> items;
    private static final int MAX_CAPACITY = 9;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Inventory() {
        items = new ArrayList<>();
        loadStartingItems();
    }

    private void loadStartingItems() {
        items.add(new Item("Health Potion",
                "Restores 50 HP",
                Item.ItemEffect.HEAL, 50,
                "assets/images/item_health_potion.png"));
        items.add(new Item("Mana Elixir",
                "Restores 40 MP (or 20 HP for non-mages)",
                Item.ItemEffect.RESTORE_MANA, 40,
                "assets/images/item_mana_elixir.png"));
        items.add(new Item("Revive Scroll",
                "Revives a dead ally at 40% HP",
                Item.ItemEffect.REVIVE, 60,
                "assets/images/item_revive_scroll.png"));
    }

    // ── Core operation ────────────────────────────────────────────────────────

    /**
     * Validates and applies the item at [index] to [target].
     * Item is only consumed (removed) if validation passes.
     *
     * @param index   0-based index in the item list
     * @param target  the Character the item is aimed at
     * @param party   full party list — needed for Revive party-wide check
     * @return        the result String from Item.applyTo() for dialog display
     * @throws EmptyInventoryException if inventory is empty
     * @throws ItemUseException        if Item.canUse() rejects the use
     */
    public String useItem(int index, Character target, ArrayList<Character> party)
            throws EmptyInventoryException, ItemUseException {

        if (items.isEmpty()) throw new EmptyInventoryException();

        if (index < 0 || index >= items.size())
            throw new EmptyInventoryException(
                "Invalid selection. Pick 1–" + items.size() + ".");

        Item chosen = items.get(index);   // peek — do NOT remove yet

        // ── Validate before consuming ─────────────────────────────────────────
        String reason = chosen.canUse(target, party);
        if (reason != null) throw new ItemUseException(reason);

        // Validation passed — now remove and apply
        items.remove(index);
        return chosen.applyTo(target);
    }

    // ── Legacy overload (no party check) — kept for backward compatibility ────
    /** @deprecated Use useItem(index, target, party) instead. */
    public void useItem(int index, Character target) throws EmptyInventoryException {
        try {
            useItem(index, target, new ArrayList<>());
        } catch (ItemUseException e) {
            // Swallow — legacy callers don't handle this
        }
    }

    // ── Add / query ───────────────────────────────────────────────────────────

    public boolean addItem(Item item) {
        if (items.size() >= MAX_CAPACITY) return false;
        items.add(item);
        return true;
    }

    public String[] getItemLabels() {
        String[] labels = new String[items.size()];
        for (int i = 0; i < items.size(); i++)
            labels[i] = (i + 1) + ". " + items.get(i).toString();
        return labels;
    }

    /** Returns item labels with a usability hint for the given character + party. */
    public String[] getItemLabelsFor(Character target, ArrayList<Character> party) {
        String[] labels = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            String reason = item.canUse(target, party);
            String mark   = (reason == null) ? "  " : "✗ ";
            labels[i] = (i + 1) + ". " + mark + item.getName()
                       + " — " + item.getDescription();
        }
        return labels;
    }

    public Item        getItem(int index)  { return items.get(index);          }
    public ArrayList<Item> getItems()      { return new ArrayList<>(items);    }
    public int         getSize()           { return items.size();               }
    public boolean     isEmpty()           { return items.isEmpty();            }
    public int         getMaxCapacity()    { return MAX_CAPACITY;               }

    public void printInventory() {
        if (items.isEmpty()) { System.out.println("  [Inventory] (empty)"); return; }
        for (int i = 0; i < items.size(); i++)
            System.out.printf("  %d. %s%n", i + 1, items.get(i));
    }
}
