/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * BattleTurnHandler.java  ← UPGRADED (Phase 5)
 * Processes a single party member's turn in the battle loop.
 *
 * CHANGES from Phase 4:
 *   ✦ Constructor now accepts the full enemy ArrayList (multi-enemy waves).
 *   ✦ Player selects which enemy to target when enemies > 1.
 *   ✦ All attacks route through CombatResolver (hit / miss / crit).
 *   ✦ Flee has a 60% success chance; on failure, PartyManager.applyFleePenalty().
 *   ✦ Taunt awareness: if actor is taunted, only Attack is available, at
 *     high miss chance via CombatResolver.resolveTaunted().
 *   ✦ Archer bleed still ticked by BattleSystem; Mage mana still tracked
 *     inside Mage — no changes needed to subclasses.
 *
 * All existing exception handling (BattleException, EmptyInventoryException,
 * general catch-all) is PRESERVED and unchanged.
 *
 * OOP Pillars:
 *   Encapsulation — private fields, one public entry point (processTurn)
 *   Polymorphism  — calls attack() / useSkill() / defend() on any Character
 */
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Random;

public class BattleTurnHandler {

    // ── Turn result enum ──────────────────────────────────────────────────────
    public enum TurnResult {
        ATTACKED,       // player used basic attack
        USED_SKILL,     // player used special skill
        USED_ITEM,      // player used an item
        DEFENDED,       // player chose to defend
        FLED,           // player successfully fled
        FLEE_FAILED,    // flee attempted but failed (penalty applied)
        INVALID_INPUT,  // bad input — turn should be retried
        CANNOT_FLEE     // player tried to flee boss — turn retried
    }

    // ── Flee probability (requirement: 60%) ──────────────────────────────────
    private static final double FLEE_SUCCESS_CHANCE  = 0.60;
    private static final double FLEE_PENALTY_PERCENT = 0.15;   // 15% maxHp per member

    // ── Private fields ────────────────────────────────────────────────────────
    private final Character         actor;          // the party member acting this turn
    private final Inventory         inventory;
    private final ArrayList<Enemy>  enemies;        // current wave's enemies (1–3)
    private final int               currentWave;
    private final PartyManager      partyManager;   // needed for flee penalty
    private final TauntState        tauntState;     // shared taunt tracker

    private boolean actorDefending = false;
    private final Random random    = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────
    /**
     * @param actor        the party Character whose turn this is
     * @param inventory    the party's shared Inventory
     * @param enemies      living enemies in the current wave
     * @param currentWave  wave number 1–6
     * @param partyManager reference for flee penalty and status display
     * @param tauntState   shared taunt tracker
     */
    public BattleTurnHandler(Character actor, Inventory inventory,
                              ArrayList<Enemy> enemies, int currentWave,
                              PartyManager partyManager, TauntState tauntState) {
        this.actor        = actor;
        this.inventory    = inventory;
        this.enemies      = enemies;
        this.currentWave  = currentWave;
        this.partyManager = partyManager;
        this.tauntState   = tauntState;
    }

    // ── Main turn processor ───────────────────────────────────────────────────

    /**
     * Processes the acting party member's chosen action.
     *
     * If the actor is taunted:
     *   - Menu is skipped; they MUST Attack (with high miss chance).
     *   - Taunt is consumed after the forced attack.
     *
     * @return TurnResult indicating what happened
     */
    public TurnResult processTurn() {
        actorDefending = false;

        // ── TAUNT OVERRIDE ────────────────────────────────────────────────────
        if (tauntState.isTaunted(actor)) {
            return processTauntedTurn();
        }

        // ── STATUS + MENU ─────────────────────────────────────────────────────
        String statusMsg = buildStatusMessage();

        String[] actions = {
            "⚔  Attack",
            "✨  Use Skill  (" + actor.getSkillName() + ")",
            "🎒  Use Item   (" + inventory.getSize() + " remaining)",
            "🛡  Defend",
            "🚪  Flee"
        };

        int choice = InputValidator.showOptionMenu(
                null, statusMsg, "LARP.exe — Wave " + currentWave
                + " [" + actor.getName() + "]", actions);

        if (choice == -1) {
            JOptionPane.showMessageDialog(null, "You must choose an action!",
                    "No Action Selected", JOptionPane.WARNING_MESSAGE);
            return TurnResult.INVALID_INPUT;
        }

        // ── Action dispatch with full exception handling ───────────────────────
        try {
            switch (choice) {

                case 0: return processAttack(false);

                case 1: return processSkill();

                case 2: return processItemUse();

                case 3: return processDefend();

                case 4: return processFlee();

                default:
                    throw new BattleException(BattleException.ErrorType.INVALID_MENU_CHOICE);
            }

        } catch (BattleException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Battle Error", JOptionPane.WARNING_MESSAGE);
            return (e.getErrorType() == BattleException.ErrorType.CANNOT_FLEE)
                    ? TurnResult.CANNOT_FLEE
                    : TurnResult.INVALID_INPUT;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unexpected error: " + e.getMessage() + "\nPlease try again.",
                    "System Error", JOptionPane.ERROR_MESSAGE);
            return TurnResult.INVALID_INPUT;
        }
    }

    // ── Taunted turn ──────────────────────────────────────────────────────────

    /**
     * Forced attack when actor is taunted.
     * Uses CombatResolver.resolveTaunted() for the 50% miss penalty.
     */
    private TurnResult processTauntedTurn() {
        JOptionPane.showMessageDialog(null,
                actor.getName() + " is TAUNTED and must attack!\n(High miss chance this turn)",
                "Taunted!", JOptionPane.WARNING_MESSAGE);

        Enemy target = pickEnemyTarget();
        if (target == null) return TurnResult.INVALID_INPUT;

        int raw = actor.attack();
        CombatResult result = CombatResolver.resolveTaunted(raw, target, false);

        tauntState.consumeTaunt();   // taunt expires after the forced action

        showResult("⚔ Forced Attack (Taunted)",
                actor.getName() + " attacks " + target.getName() + "\n"
                + result.getLabel() + "\n"
                + target.getName() + " HP: " + target.getHp() + "/" + target.getMaxHp());

        return TurnResult.ATTACKED;
    }

    // ── Attack ────────────────────────────────────────────────────────────────

    /**
     * Handles a Basic Attack — routes through CombatResolver for hit/miss/crit.
     *
     * @param forcedTaunt if true, caller already verified this is a taunt turn
     *                    (kept for API compatibility; use processTauntedTurn instead)
     */
    private TurnResult processAttack(boolean forcedTaunt) {
        Enemy target = pickEnemyTarget();
        if (target == null) return TurnResult.INVALID_INPUT;

        int raw = actor.attack();
        CombatResult result = CombatResolver.resolve(raw, target, false);

        showResult("⚔ Attack",
                actor.getName() + " attacks " + target.getName() + "\n"
                + result.getLabel() + "\n"
                + target.getName() + " HP: " + target.getHp() + "/" + target.getMaxHp());

        return TurnResult.ATTACKED;
    }

    // ── Skill ─────────────────────────────────────────────────────────────────

    private TurnResult processSkill() {
        Enemy target = pickEnemyTarget();
        if (target == null) return TurnResult.INVALID_INPUT;

        int raw = actor.useSkill();
        CombatResult result = CombatResolver.resolve(raw, target, false);

        showResult("✨ Skill: " + actor.getSkillName(),
                actor.getName() + " uses their skill on " + target.getName() + "\n"
                + result.getLabel() + "\n"
                + target.getName() + " HP: " + target.getHp() + "/" + target.getMaxHp());

        return TurnResult.USED_SKILL;
    }

    // ── Defend ────────────────────────────────────────────────────────────────

    private TurnResult processDefend() {
        int blocked = actor.defend();
        actorDefending = true;
        showResult("🛡 Defend",
                actor.getName() + " takes a defensive stance!\n"
                + "Incoming damage reduced by up to " + (blocked > 0 ? blocked : "???") + ".");
        return TurnResult.DEFENDED;
    }

    // ── Flee ──────────────────────────────────────────────────────────────────

    /**
     * Flee attempt with 60% success chance.
     * Throws BattleException on boss wave (existing exception system preserved).
     * On failure: PartyManager.applyFleePenalty() damages all living members.
     */
    private TurnResult processFlee() throws BattleException {
        InputValidator.validateFleeAttempt(currentWave);   // throws if boss wave

        boolean success = (random.nextDouble() < FLEE_SUCCESS_CHANCE);
        if (success) {
            showResult("🚪 Flee",
                    actor.getName() + " escapes from the battle!\nYou lived to fight another day...");
            return TurnResult.FLED;
        } else {
            // Flee failed — apply 15% maxHp penalty to all living members
            partyManager.applyFleePenalty(FLEE_PENALTY_PERCENT);
            return TurnResult.FLEE_FAILED;
        }
    }

    // ── Item use ──────────────────────────────────────────────────────────────

    /**
     * Handles the item selection sub-menu.
     * Catches EmptyInventoryException independently (requirement preserved).
     */
    /**
     * Full item use flow — fixed in Phase 6.
     *
     * Step 1: Pick WHICH item (shown with ✗ markers for unusable ones).
     * Step 2: Pick WHO to use it on:
     *           - HEAL / RESTORE_MANA → any living party member
     *           - REVIVE              → any DEAD party member
     *             (falls back to living target only if nobody is dead,
     *              and only if target is injured — otherwise blocked)
     * Step 3: Call Item.canUse() — if blocked, show why and DO NOT consume.
     * Step 4: Apply and show result string from Item.applyTo().
     *
     * The item is only removed from inventory after all checks pass.
     */
    private TurnResult processItemUse() {
        try {
            if (inventory.isEmpty()) throw new EmptyInventoryException();

            ArrayList<Character> fullParty = partyManager.getParty();

            // ── Step 1: choose item ───────────────────────────────────────────
            // Show labels with usability markers relative to the actor so the
            // player has a hint, even though target is chosen next.
            String[] itemLabels = inventory.getItemLabelsFor(actor, fullParty);
            int itemChoice = InputValidator.showOptionMenu(
                    null,
                    "🎒 ITEM BAG\n\n" + partyManager.getPartyStatusBlock()
                    + "\n\nChoose an item:  (✗ = may not be usable on current target)",
                    "Use Item — Wave " + currentWave,
                    itemLabels);

            if (itemChoice == -1) return TurnResult.INVALID_INPUT;

            Item chosen = inventory.getItem(itemChoice);

            // ── Step 2: choose target ─────────────────────────────────────────
            Character target = pickItemTarget(chosen, fullParty);
            if (target == null) return TurnResult.INVALID_INPUT;  // player cancelled

            // ── Step 3: validate (item stays in bag if blocked) ───────────────
            String reason = chosen.canUse(target, fullParty);
            if (reason != null) {
                JOptionPane.showMessageDialog(null,
                        "⚠ Can't use " + chosen.getName() + " on " + target.getName() + "!\n\n"
                        + reason + "\n\nItem was NOT consumed.",
                        "Item Blocked", JOptionPane.WARNING_MESSAGE);
                return TurnResult.INVALID_INPUT;   // turn retried, item intact
            }

            // ── Step 4: apply ─────────────────────────────────────────────────
            String resultMsg = inventory.useItem(itemChoice, target, fullParty);
            showResult("🎒 " + chosen.getName(), resultMsg + "\n\n" + partyManager.getPartyStatusBlock());
            return TurnResult.USED_ITEM;

        } catch (EmptyInventoryException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(), "Inventory Empty", JOptionPane.WARNING_MESSAGE);
            return TurnResult.INVALID_INPUT;

        } catch (Inventory.ItemUseException e) {
            // Defensive catch — should not reach here because we call canUse() above,
            // but Inventory.useItem() also checks as a safety net.
            JOptionPane.showMessageDialog(null,
                    "⚠ " + e.getMessage() + "\n\nItem was NOT consumed.",
                    "Item Blocked", JOptionPane.WARNING_MESSAGE);
            return TurnResult.INVALID_INPUT;
        }
    }

    /**
     * Picks the CHARACTER target for an item, depending on item type.
     *
     *  HEAL / RESTORE_MANA → list of LIVING party members to pick from.
     *  REVIVE              → list of DEAD party members (because that's the
     *                        primary use case). If nobody is dead, falls back
     *                        to living members so the scroll can still heal.
     *
     * Returns null if player cancelled.
     */
    private Character pickItemTarget(Item item, ArrayList<Character> fullParty) {
        ArrayList<Character> candidates = new ArrayList<>();

        if (item.getEffect() == Item.ItemEffect.REVIVE) {
            // Prefer dead members as revive targets
            for (Character c : fullParty) if (c.isDefeated())  candidates.add(c);
            // Fallback: if nobody dead, let them heal an injured living member
            if (candidates.isEmpty())
                for (Character c : fullParty) if (!c.isDefeated()) candidates.add(c);
        } else {
            // Heal / Mana — only living members make sense
            for (Character c : fullParty) if (!c.isDefeated()) candidates.add(c);
        }

        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);

        // Build selection labels with HP/MP context
        String[] labels = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            Character c = candidates.get(i);
            String hpLine = "HP " + c.getHp() + "/" + c.getMaxHp();
            String mpLine = (c instanceof Mage)
                    ? "  MP " + ((Mage)c).getMana() + "/" + ((Mage)c).getMaxMana()
                    : "";
            String deadMark = c.isDefeated() ? " ✝ DEFEATED" : "";
            labels[i] = c.getName() + "  [" + hpLine + mpLine + deadMark + "]";
        }

        int choice = InputValidator.showOptionMenu(
                null,
                "Use " + item.getName() + " on who?",
                "Choose Target",
                labels);

        if (choice == -1) return null;
        return candidates.get(choice);
    }

    // ── Enemy targeting ───────────────────────────────────────────────────────

    /**
     * If only one living enemy exists, returns it immediately.
     * Otherwise shows a JOptionPane letting the player pick a target.
     *
     * @return the chosen Enemy, or null if player cancelled
     */
    private Enemy pickEnemyTarget() {
        ArrayList<Enemy> living = new ArrayList<>();
        for (Enemy e : enemies) {
            if (!e.isDefeated()) living.add(e);
        }

        if (living.isEmpty()) return null;
        if (living.size() == 1) return living.get(0);

        // Build target labels
        String[] labels = new String[living.size()];
        for (int i = 0; i < living.size(); i++) {
            Enemy e = living.get(i);
            labels[i] = e.getName() + "  [HP " + e.getHp() + "/" + e.getMaxHp() + "]";
        }

        int choice = InputValidator.showOptionMenu(
                null, "Choose a target:", "Select Target", labels);

        if (choice == -1) return null;
        return living.get(choice);
    }

    // ── Status builder ────────────────────────────────────────────────────────

    private String buildStatusMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(partyManager.getPartyStatusBlock()).append("\n\n");
        sb.append("  ENEMIES\n");
        for (Enemy e : enemies) {
            if (!e.isDefeated()) {
                sb.append("  ").append(e.getStatusLine())
                  .append(e.isBuffed() ? "  [BUFFED]" : "").append("\n");
            }
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        if (tauntState.isTaunted(actor)) {
            sb.append("⚠ YOU ARE TAUNTED — must Attack!\n");
        }
        sb.append("Choose your action:");
        return sb.toString();
    }

    private void showResult(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Getter ────────────────────────────────────────────────────────────────
    public boolean isActorDefending() { return actorDefending; }
}
