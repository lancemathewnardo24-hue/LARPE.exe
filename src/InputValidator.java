/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * InputValidator.java
 * Centralized utility for all user input validation in the game.
 * Every menu selection, integer parse, and range check goes through here.
 *
 * This is where the requirement is fully satisfied:
 *   "Invalid menu selections must be caught with a try-catch block"
 *   "Handle runtime exceptions cleanly"
 *
 * All methods are static — no instantiation needed.
 * Used by BattleTurnHandler and all future JOptionPane menus in Phase 5.
 */
import javax.swing.JOptionPane;

public class InputValidator {

    // ── Private constructor — utility class, not instantiated ─────────────────
    private InputValidator() {}

    // ── Console input validation ──────────────────────────────────────────────

    /**
     * Parses a string as an integer and validates it is within [min, max].
     * Throws BattleException if the string is not a number or out of range.
     *
     * @param input the raw string from the user
     * @param min   minimum valid value (inclusive)
     * @param max   maximum valid value (inclusive)
     * @return the validated integer
     * @throws BattleException if input is non-numeric or out of range
     */
    public static int parseMenuChoice(String input, int min, int max)
            throws BattleException {
        try {
            int choice = Integer.parseInt(input.trim());
            if (choice < min || choice > max) {
                throw new BattleException(
                        BattleException.ErrorType.INVALID_MENU_CHOICE,
                        "Please enter a number between " + min + " and " + max + "."
                );
            }
            return choice;
        } catch (NumberFormatException e) {
            throw new BattleException(
                    BattleException.ErrorType.INVALID_MENU_CHOICE,
                    "\"" + input.trim() + "\" is not a valid number. Enter " + min + "–" + max + "."
            );
        }
    }

    // ── JOptionPane input validation (used in Phase 5 GUI) ───────────────────

    /**
     * Shows a JOptionPane input dialog and validates the result is an integer
     * within [min, max]. Re-prompts the user until valid input is given or
     * they cancel.
     *
     * @param parentComponent parent JFrame (can be null)
     * @param prompt          the message shown to the user
     * @param title           the dialog title
     * @param min             minimum valid value
     * @param max             maximum valid value
     * @return the validated integer, or -1 if the user cancelled
     */
    public static int getValidMenuChoice(java.awt.Component parentComponent,
                                         String prompt, String title,
                                         int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    parentComponent, prompt, title, JOptionPane.QUESTION_MESSAGE);

            // User pressed Cancel or closed the dialog
            if (input == null) return -1;

            try {
                int choice = parseMenuChoice(input, min, max);
                return choice;
            } catch (BattleException e) {
                JOptionPane.showMessageDialog(
                        parentComponent,
                        e.getMessage(),
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                );
                // Loop continues — re-prompts the user
            }
        }
    }

    /**
     * Shows a JOptionPane with radio-button style options (showOptionDialog).
     * Returns the index of the chosen option, or -1 if cancelled.
     * This is the primary battle menu method used in Phase 5.
     *
     * @param parentComponent parent JFrame
     * @param message         the question or prompt shown above the buttons
     * @param title           the dialog window title
     * @param options         the button labels to show
     * @return 0-based index of the chosen option, or -1 if cancelled
     */
    public static int showOptionMenu(java.awt.Component parentComponent,
                                      String message, String title,
                                      String[] options) {
        int result = JOptionPane.showOptionDialog(
                parentComponent,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        return result; // -1 if closed, otherwise 0-based index
    }

    /**
     * Validates a flee attempt — throws BattleException if the current
     * wave is the boss wave (wave 6) where fleeing is disabled.
     *
     * @param currentWave the current wave number (1–6)
     * @throws BattleException if currentWave == 6 (boss fight)
     */
    public static void validateFleeAttempt(int currentWave) throws BattleException {
        if (currentWave >= 6) {
            throw new BattleException(BattleException.ErrorType.CANNOT_FLEE);
        }
    }

    // ── String safety helpers ─────────────────────────────────────────────────

    /**
     * Returns true if the string is non-null and non-empty after trimming.
     * Used when validating player name input on the title screen.
     */
    public static boolean isValidName(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Sanitizes player name input — trims whitespace, caps at 16 characters.
     */
    public static String sanitizeName(String input) {
        if (input == null) return "Hero";
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "Hero";
        return trimmed.length() > 16 ? trimmed.substring(0, 16) : trimmed;
    }
}
