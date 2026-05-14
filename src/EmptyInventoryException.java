/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * EmptyInventoryException.java
 * Custom checked exception thrown when the player tries to use an item
 * but their inventory is empty.
 *
 * OOP Pillars covered:
 *   Inheritance — extends Exception (Java's built-in exception hierarchy)
 *
 * Requirement met:
 *   "Students must implement at least one custom Exception class"
 */
public class EmptyInventoryException extends Exception {

    // ── Constructor ───────────────────────────────────────────────────────────
    public EmptyInventoryException() {
        super("Your inventory is empty! No items left to use.");
    }

    /**
     * Overloaded constructor for a custom message.
     * Useful if you want context-specific error messages in the UI.
     *
     * @param message a specific message describing the empty inventory situation
     */
    public EmptyInventoryException(String message) {
        super(message);
    }
}
