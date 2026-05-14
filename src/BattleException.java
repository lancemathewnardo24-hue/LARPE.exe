/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * BattleException.java
 * Custom exception for illegal or impossible battle actions.
 * Thrown when the player attempts something invalid mid-battle
 * (e.g. selecting a non-existent menu option, fleeing a boss fight).
 *
 * OOP Pillars covered:
 *   Inheritance — extends Exception
 *
 * Separate from EmptyInventoryException to keep error types distinct
 * and easy to catch independently in the battle loop.
 */
public class BattleException extends Exception {

    // ── Error type classification ─────────────────────────────────────────────
    public enum ErrorType {
        INVALID_MENU_CHOICE,   // player typed something that isn't on the menu
        CANNOT_FLEE,           // player tried to flee a no-escape fight (boss)
        SKILL_NOT_READY,       // future use: cooldown system
        INVALID_TARGET         // future use: multi-enemy targeting
    }

    private final ErrorType errorType;

    // ── Constructors ──────────────────────────────────────────────────────────
    public BattleException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public BattleException(ErrorType errorType) {
        super(getDefaultMessage(errorType));
        this.errorType = errorType;
    }

    // ── Default messages per error type ───────────────────────────────────────
    private static String getDefaultMessage(ErrorType type) {
        switch (type) {
            case INVALID_MENU_CHOICE:
                return "Invalid choice! Please select a valid menu option.";
            case CANNOT_FLEE:
                return "You cannot flee from this battle! Face your enemy!";
            case SKILL_NOT_READY:
                return "Your skill is not ready yet!";
            case INVALID_TARGET:
                return "Invalid target selected.";
            default:
                return "An unknown battle error occurred.";
        }
    }

    // ── Getter ────────────────────────────────────────────────────────────────
    public ErrorType getErrorType() { return errorType; }
}
