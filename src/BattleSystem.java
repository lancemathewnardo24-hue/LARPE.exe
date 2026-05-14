/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * BattleSystem.java  ← UPDATED (Phase 6)
 *
 * CHANGES:
 *   ✦ Added CurrencySystem — gold awarded every time an enemy dies.
 *   ✦ runWave() detects FLED result → calls TownScreen.open() → rebuilds
 *     the same wave fresh so the player re-fights it after returning.
 *   ✦ Gold awarded per kill during combat via awardGoldForDefeated().
 *   ✦ After each WAVE CLEAR (not just flee), player gets a between-wave
 *     choice: proceed to next wave, or visit town first.
 *   ✦ Score tracking: enemiesDefeated, totalTurns, wavesCleared.
 *
 * Flow:
 *   start()
 *     → showIntro()
 *     → loop waves:
 *         showWaveAnnouncement()
 *         runWave()          ← returns SURVIVED, WIPED, or FLED_TO_TOWN
 *           if FLED_TO_TOWN: TownScreen.open() → rebuild wave → runWave() again
 *           if SURVIVED:     grantDrops() → offerTownBetweenWaves()
 *           if WIPED:        break
 *     → showEndScreen()
 */
import javax.swing.JOptionPane;
import java.util.ArrayList;

public class BattleSystem {

    // ── Wave run outcomes ─────────────────────────────────────────────────────
    private enum WaveOutcome { SURVIVED, WIPED, FLED_TO_TOWN }

    // ── Core components ───────────────────────────────────────────────────────
    private final PartyManager   partyManager;
    private final WaveManager    waveManager;
    private final TauntState     tauntState;
    private final CurrencySystem currency;     // ← NEW

    // ── Score tracking ────────────────────────────────────────────────────────
    private int enemiesDefeated = 0;
    private int totalTurns      = 0;
    private int wavesCleared    = 0;

    // ── Constructor ───────────────────────────────────────────────────────────
    public BattleSystem(String playerName) {
        this.partyManager = new PartyManager(playerName);
        this.waveManager  = new WaveManager();
        this.tauntState   = new TauntState();
        this.currency     = new CurrencySystem();
    }

    // ── Main entry ────────────────────────────────────────────────────────────
    public void start() {
        showIntro();

        while (waveManager.hasMoreWaves() && !partyManager.isWiped()) {
            int waveNum = waveManager.getCurrentWave() + 1;   // peek next wave number
            ArrayList<Enemy> wave = waveManager.nextWave();

            showWaveAnnouncement(waveNum, wave);

            // ── Wave loop: re-run the same wave if player flees and returns ───
            WaveOutcome outcome;
            do {
                outcome = runWave(wave, waveNum);

                if (outcome == WaveOutcome.FLED_TO_TOWN) {
                    // Open town hub — player can shop / use items / rest
                    TownScreen.open(partyManager, currency, waveNum);

                    if (partyManager.isWiped()) break;   // edge case: died in town somehow

                    // Rebuild the wave fresh (enemies were still alive when they fled)
                    wave = waveManager.peekWave(waveNum);
                    showMessage("⚔ Back to Wave " + waveNum,
                            "You return to the fight!\n\n" + partyManager.getPartyStatusBlock());
                }

            } while (outcome == WaveOutcome.FLED_TO_TOWN && !partyManager.isWiped());

            if (outcome == WaveOutcome.WIPED || partyManager.isWiped()) break;

            // ── Wave cleared ──────────────────────────────────────────────────
            wavesCleared++;
            waveManager.grantDrops(partyManager.getInventory(), waveNum);

            // Between-wave town option (only if more waves remain)
            if (waveManager.hasMoreWaves()) {
                offerTownBetweenWaves(waveNum);
            }
        }

        // ── End game ─────────────────────────────────────────────────────────
        if (partyManager.isWiped()) {
            showEndScreen(false);
        } else {
            showEndScreen(true);
        }
    }

    // ── Wave runner ───────────────────────────────────────────────────────────

    /**
     * Runs one full wave of combat.
     * Each round: party acts → check enemies → enemies act → check party.
     *
     * Returns:
     *   SURVIVED    — all enemies defeated, party alive
     *   WIPED       — all party members dead
     *   FLED_TO_TOWN — player successfully fled; wave must be restarted
     */
    private WaveOutcome runWave(ArrayList<Enemy> enemies, int waveNum) {
        while (!allDefeated(enemies) && !partyManager.isWiped()) {

            // ── PARTY TURN ────────────────────────────────────────────────────
            // Let the player choose WHICH party member acts each sub-turn
            ArrayList<Character> living = partyManager.getLivingMembers();
            for (int i = 0; i < living.size(); i++) {
                if (allDefeated(enemies)) break;

                // Player selects who acts this sub-turn
                Character actor = partyManager.selectActingMember(waveNum);
                if (actor == null) continue;   // cancelled — re-prompt same turn

                totalTurns++;

                BattleTurnHandler handler = new BattleTurnHandler(
                        actor,
                        partyManager.getInventory(),
                        enemies,
                        waveNum,
                        partyManager,
                        tauntState
                );

                BattleTurnHandler.TurnResult result = handler.processTurn();

                // ── Flee success ──────────────────────────────────────────────
                if (result == BattleTurnHandler.TurnResult.FLED) {
                    return WaveOutcome.FLED_TO_TOWN;
                }

                // ── Award gold for any enemies that just died ─────────────────
                awardGoldForDefeated(enemies);

                // ── Archer bleed tick ─────────────────────────────────────────
                if (actor instanceof Archer) {
                    Archer archer = (Archer) actor;
                    if (archer.hasBleed()) {
                        for (Enemy e : enemies) {
                            if (!e.isDefeated()) {
                                int bleedDmg = archer.tickBleed();
                                if (bleedDmg > 0) {
                                    e.setHp(e.getHp() - bleedDmg);
                                    // Re-check gold in case bleed finished them
                                    awardGoldForDefeated(enemies);
                                }
                            }
                        }
                    }
                }

                // One sub-turn per living member per round — break after everyone went
                // (selectActingMember lets them CHOOSE who, not forced order)
            }
            // All living members have acted — move to enemy turn

            if (allDefeated(enemies)) break;

            // ── ENEMY TURN ────────────────────────────────────────────────────
            EnemyAI.executeEnemyTurns(enemies, partyManager.getParty(), tauntState);
            tauntState.tick();

            if (partyManager.isWiped()) return WaveOutcome.WIPED;
        }

        return partyManager.isWiped() ? WaveOutcome.WIPED : WaveOutcome.SURVIVED;
    }

    // ── Between-wave town offer ────────────────────────────────────────────────

    /**
     * After clearing a wave (but before the next), asks the player:
     *   "Proceed to Wave N+1" or "Visit Town first"
     */
    private void offerTownBetweenWaves(int justClearedWave) {
        int nextWave = justClearedWave + 1;
        String[] options = {
            "⚔  Proceed to Wave " + nextWave,
            "🏘  Visit Town first  (💰 " + currency.getGold() + "g)"
        };

        int choice = JOptionPane.showOptionDialog(
                null,
                "Wave " + justClearedWave + " cleared! ✅\n\n"
                + partyManager.getPartyStatusBlock() + "\n"
                + "💰 Gold: " + currency.getGold() + "g\n\n"
                + "What would you like to do before Wave " + nextWave + "?",
                "Wave " + justClearedWave + " Clear!",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        // -1 (closed) or 0 = proceed directly
        if (choice == 1) {
            TownScreen.open(partyManager, currency, nextWave);
        }
    }

    // ── Gold on kill ──────────────────────────────────────────────────────────

    /**
     * Scans enemies for any that are newly defeated and awards gold.
     * Uses a simple "was alive, now dead" check — tracks via a flag on Enemy.
     * Called after every player action that could kill an enemy.
     */
    private void awardGoldForDefeated(ArrayList<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.isDefeated() && !e.hasBeenRewarded()) {
                int gold = currency.awardForKill(e.getEnemyType());
                enemiesDefeated++;
                e.markRewarded();
                JOptionPane.showMessageDialog(null,
                        "💀 " + e.getName() + " defeated!\n"
                        + "+" + gold + "g  (Total: " + currency.getGold() + "g)",
                        "Enemy Defeated", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // ── End screen ────────────────────────────────────────────────────────────

    private void showEndScreen(boolean won) {
        String title = won ? "🏆 VICTORY!" : "☠ DEFEAT";
        String header = won
                ? "The LARP Overlord has been vanquished!\nYou are the ultimate LARPer.\n\n"
                : "Your entire party has been defeated...\n\n";

        String score =
                "━━━━━━ FINAL SCORE ━━━━━━\n"
                + "  Enemies defeated : " + enemiesDefeated + "\n"
                + "  Turns taken      : " + totalTurns      + "\n"
                + "  Waves cleared    : " + wavesCleared + " / " + WaveManager.TOTAL_WAVES + "\n"
                + "  Gold earned      : " + currency.getGold() + "g\n"
                + "━━━━━━━━━━━━━━━━━━━━━━━━";

        JOptionPane.showMessageDialog(null,
                header + score,
                title,
                won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean allDefeated(ArrayList<Enemy> enemies) {
        for (Enemy e : enemies) if (!e.isDefeated()) return false;
        return true;
    }

    private void showIntro() {
        JOptionPane.showMessageDialog(null,
                "LARP.exe: Break the Illusion\n\n"
                + "Your party of three LARPers enters the convention floor.\n"
                + "Six waves stand between you and glory.\n\n"
                + partyManager.getPartyStatusBlock(),
                "Battle Start!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWaveAnnouncement(int waveNum, ArrayList<Enemy> wave) {
        StringBuilder msg = new StringBuilder();
        msg.append(waveNum == WaveManager.BOSS_WAVE
                ? "⚠ BOSS WAVE — No escape!\n\n"
                : "Wave " + waveNum + " of " + WaveManager.TOTAL_WAVES + "\n\n");
        msg.append("Enemies approaching:\n");
        for (Enemy e : wave) {
            msg.append("  • ").append(e.getName())
               .append("  [HP ").append(e.getMaxHp()).append("]")
               .append("  💰 ").append(CurrencySystem.rewardFor(e.getEnemyType())).append("g\n");
        }
        JOptionPane.showMessageDialog(null, msg.toString().trim(),
                "Wave " + waveNum, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMessage(String title, String body) {
        JOptionPane.showMessageDialog(null, body, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
