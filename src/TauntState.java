/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * TauntState.java
 * Tracks which party member is currently taunted and for how many turns.
 *
 * Taunt mechanic (NEW):
 *   - Applied by enemy AI when it chooses TAUNT action
 *   - Has a SUCCESS_CHANCE (40 %) roll
 *   - If successful, the targeted party member MUST use Basic Attack next turn
 *   - That Basic Attack has HIGH miss chance (50 %)
 *   - Lasts 1 turn
 *
 * Used by: BattleSystem (checks before allowing action selection)
 *          EnemyAI      (applies taunt)
 *
 * OOP: Encapsulation — all state is private, mutated through methods only.
 */
public class TauntState {

    /** Probability that a taunt attempt actually lands. */
    public static final double SUCCESS_CHANCE = 0.40;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean        active          = false;
    private Character      taunted         = null;   // which party member is taunted
    private int            turnsRemaining  = 0;

    // ── Apply ─────────────────────────────────────────────────────────────────

    /**
     * Attempts to apply a taunt to the target character.
     *
     * @param target the party Character being taunted
     * @return true if the taunt succeeded
     */
    public boolean tryApply(Character target) {
        boolean success = (Math.random() < SUCCESS_CHANCE);
        if (success) {
            active         = true;
            taunted        = target;
            turnsRemaining = 1;
            System.out.println("  [Taunt] " + target.getName()
                    + " is TAUNTED! Must attack next turn (high miss chance).");
        } else {
            System.out.println("  [Taunt] Taunt attempt resisted by "
                    + target.getName() + "!");
        }
        return success;
    }

    // ── Consume / Tick ────────────────────────────────────────────────────────

    /**
     * Called after the taunted character has used their forced Basic Attack.
     * Clears the taunt state immediately (1-turn duration).
     */
    public void consumeTaunt() {
        active         = false;
        taunted        = null;
        turnsRemaining = 0;
    }

    /**
     * Decrements taunt timer. If it expires naturally, clears state.
     * Call at the end of the affected character's turn.
     */
    public void tick() {
        if (active && turnsRemaining > 0) {
            turnsRemaining--;
            if (turnsRemaining == 0) {
                active  = false;
                taunted = null;
            }
        }
    }

    // ── Query ─────────────────────────────────────────────────────────────────
    public boolean   isActive()           { return active;          }
    public Character getTaunted()         { return taunted;         }
    public boolean   isTaunted(Character c) { return active && taunted == c; }
}
