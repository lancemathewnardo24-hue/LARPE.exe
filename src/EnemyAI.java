/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * EnemyAI.java
 * Drives enemy turn execution.  For each living enemy in the wave,
 * EnemyAI calls decideAction() and then applies the chosen effect.
 *
 * Supported AI Actions (requirement):
 *   ATTACK  — standard attack against a random living party member
 *   BUFF    — enemy buffs its own ATK/DEF for 2 turns
 *   TAUNT   — attempts to taunt a random living party member
 *
 * Integration points:
 *   - Called by BattleSystem after ALL party members have acted
 *   - Uses CombatResolver for hit/miss/crit on enemy attacks
 *   - Uses TauntState to track taunt application
 *
 * NEW class.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;

public class EnemyAI {

    private static final Random RANDOM = new Random();

    // ── Buff constants ────────────────────────────────────────────────────────
    private static final int BUFF_ATK_BONUS  = 6;
    private static final int BUFF_DEF_BONUS  = 4;
    private static final int BUFF_DURATION   = 2;

    // ── Private constructor ───────────────────────────────────────────────────
    private EnemyAI() {}

    // ── Main entry point ──────────────────────────────────────────────────────

    /**
     * Executes one full enemy "team turn": every living enemy in the wave acts.
     * Results are displayed via JOptionPane summary dialog.
     *
     * @param enemies      the list of living enemies in the current wave
     * @param party        the player's party (used as attack targets)
     * @param tauntState   shared taunt state object — modified in place
     */
    public static void executeEnemyTurns(ArrayList<Enemy> enemies,
                                         ArrayList<Character> party,
                                         TauntState tauntState) {
        StringBuilder log = new StringBuilder();
        log.append("━━━━━ ENEMY TURN ━━━━━\n\n");

        for (Enemy enemy : enemies) {
            if (enemy.isDefeated()) continue;

            // Tick buff timer before acting
            enemy.tickBuff();

            // Also tick Archer bleed if applicable (handled in BattleSystem)
            Enemy.AIAction action = enemy.decideAction();
            log.append(resolveEnemyAction(enemy, action, party, tauntState));
            log.append("\n");
        }

        // Show consolidated enemy-turn results in one dialog
        JOptionPane.showMessageDialog(null, log.toString().trim(),
                "Enemy Actions", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Action resolution ─────────────────────────────────────────────────────

    private static String resolveEnemyAction(Enemy enemy, Enemy.AIAction action,
                                             ArrayList<Character> party,
                                             TauntState tauntState) {
        List<Character> living = getLivingMembers(party);
        if (living.isEmpty()) return enemy.getName() + " has no targets.";

        switch (action) {

            // ── ATTACK ───────────────────────────────────────────────────────
            case ATTACK: {
                Character target = pickRandom(living);
                int raw = enemy.attack();
                CombatResult result = CombatResolver.resolve(raw, target, false);
                return enemy.getName() + " → " + target.getName() + "\n"
                     + "  " + result.getLabel() + "\n"
                     + "  " + target.getName() + " HP: "
                     + target.getHp() + "/" + target.getMaxHp();
            }

            // ── BUFF ─────────────────────────────────────────────────────────
            case BUFF: {
                enemy.applyBuff(BUFF_ATK_BONUS, BUFF_DEF_BONUS, BUFF_DURATION);
                return enemy.getName() + " powers up!\n"
                     + "  ATK+" + BUFF_ATK_BONUS + " DEF+" + BUFF_DEF_BONUS
                     + " for " + BUFF_DURATION + " turns.";
            }

            // ── TAUNT ────────────────────────────────────────────────────────
            case TAUNT: {
                // Only taunt if no taunt is already active
                if (tauntState.isActive()) {
                    // Taunt already in effect — fall back to basic attack instead
                    Character target = pickRandom(living);
                    int raw = enemy.attack();
                    CombatResult result = CombatResolver.resolve(raw, target, false);
                    return enemy.getName() + " (taunt already active — attacks instead)\n"
                         + "  → " + target.getName() + ": " + result.getLabel();
                }
                Character target = pickRandom(living);
                boolean success = tauntState.tryApply(target);
                return enemy.getName() + " attempts TAUNT on " + target.getName() + "!\n"
                     + "  " + (success
                         ? "✗ " + target.getName() + " is TAUNTED! (forced attack + 50% miss)"
                         : "✓ Taunt RESISTED!");
            }

            default:
                return enemy.getName() + " hesitates.";
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<Character> getLivingMembers(ArrayList<Character> party) {
        List<Character> alive = new ArrayList<>();
        for (Character c : party) {
            if (!c.isDefeated()) alive.add(c);
        }
        return alive;
    }

    private static Character pickRandom(List<Character> living) {
        return living.get(RANDOM.nextInt(living.size()));
    }
}
