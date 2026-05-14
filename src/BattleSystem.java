/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * BattleSystem.java  ← FULLY WIRED TO GUI
 *
 * Every state change now pushes to BattleScreen instead of JOptionPane.
 * JOptionPanes are kept ONLY where the requirement explicitly calls for them
 * (town/shop menus, wave-clear choice, item drop announcements).
 *
 * BattleScreen is obtained via GameWindow.getInstance().
 * BattleSystem runs on a background thread (started by BattleScreen.init()).
 *
 * GUI updates must always be dispatched on the EDT via SwingUtilities.invokeLater.
 */
import javax.swing.*;
import java.util.ArrayList;

public class BattleSystem {

    private enum WaveOutcome { SURVIVED, WIPED, FLED_TO_TOWN }

    private final PartyManager   partyManager;
    private final WaveManager    waveManager;
    private final TauntState     tauntState;
    private final CurrencySystem currency;

    private int enemiesDefeated = 0;
    private int totalTurns      = 0;
    private int wavesCleared    = 0;

    // ── Reference to the live BattleScreen ───────────────────────────────────
    private BattleScreen screen() {
        GameWindow gw = GameWindow.getInstance();
        return (gw != null) ? gw.getBattleScreen() : null;
    }

    // ── Push dialog text to screen (thread-safe) ──────────────────────────────
    private void say(String text) {
        BattleScreen s = screen();
        if (s != null) SwingUtilities.invokeLater(() -> s.setDialogText(text));
        sleep(120);   // short pause so the player can read it
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public BattleSystem(String playerName) {
        this.partyManager = new PartyManager(playerName);
        this.waveManager  = new WaveManager();
        this.tauntState   = new TauntState();
        this.currency     = new CurrencySystem();
    }

    // ── Main entry (runs on background thread) ────────────────────────────────
    public void start() {
        pushScreenState(1, new ArrayList<>());
        say("Your party enters the convention floor. Six waves await!");
        sleep(800);

        while (waveManager.hasMoreWaves() && !partyManager.isWiped()) {
            int waveNum = waveManager.getCurrentWave() + 1;
            ArrayList<Enemy> wave = waveManager.nextWave();

            pushScreenState(waveNum, wave);
            announceWave(waveNum, wave);

            WaveOutcome outcome;
            do {
                outcome = runWave(wave, waveNum);

                if (outcome == WaveOutcome.FLED_TO_TOWN) {
                    SwingUtilities.invokeLater(() -> screen().setButtonsEnabled(false));
                    TownScreen.open(partyManager, currency, waveNum);
                    if (partyManager.isWiped()) break;
                    wave = waveManager.peekWave(waveNum);
                    pushScreenState(waveNum, wave);
                    say("Back to Wave " + waveNum + "! Enemies have reset.");
                    sleep(600);
                    SwingUtilities.invokeLater(() -> screen().setButtonsEnabled(true));
                }
            } while (outcome == WaveOutcome.FLED_TO_TOWN && !partyManager.isWiped());

            if (outcome == WaveOutcome.WIPED || partyManager.isWiped()) break;

            wavesCleared++;
            waveManager.grantDrops(partyManager.getInventory(), waveNum);
            if (waveManager.hasMoreWaves()) offerTownBetweenWaves(waveNum);
        }

        SwingUtilities.invokeLater(() -> {
            if (screen() != null) screen().setButtonsEnabled(false);
        });
        showEndScreen(partyManager.isWiped() ? false : true);
    }

    // ── Push all state to BattleScreen at once ────────────────────────────────
    private void pushScreenState(int waveNum, ArrayList<Enemy> enemies) {
        BattleScreen s = screen();
        if (s == null) return;
        SwingUtilities.invokeLater(() -> {
            s.setCurrentWave(waveNum);
            s.setCurrentEnemies(enemies);
            s.setPartyManager(partyManager);
            s.setGold(currency.getGold());
        });
    }

    // ── Wave runner ───────────────────────────────────────────────────────────
    private WaveOutcome runWave(ArrayList<Enemy> enemies, int waveNum) {
        while (!allDefeated(enemies) && !partyManager.isWiped()) {

            ArrayList<Character> living = partyManager.getLivingMembers();
            for (int i = 0; i < living.size(); i++) {
                if (allDefeated(enemies)) break;

                // Show "who acts?" prompt and update active slot arrow
                Character actor = partyManager.selectActingMember(waveNum);
                if (actor == null) continue;

                // Update active slot indicator on screen
                int slot = partyManager.getParty().indexOf(actor);
                SwingUtilities.invokeLater(() -> {
                    if (screen() != null) screen().setActiveSlot(slot);
                });

                say(actor.getName() + " — choose your action!");
                totalTurns++;

                BattleTurnHandler handler = new BattleTurnHandler(
                        actor, partyManager.getInventory(),
                        enemies, waveNum, partyManager, tauntState);

                BattleTurnHandler.TurnResult result = handler.processTurn();

                // Reflect the action result as dialog text
                reflectResult(result, actor);

                if (result == BattleTurnHandler.TurnResult.FLED) {
                    say(actor.getName() + " led the party to safety!");
                    return WaveOutcome.FLED_TO_TOWN;
                }

                awardGoldForDefeated(enemies);
                pushScreenState(waveNum, enemies);

                // Archer bleed
                if (actor instanceof Archer) {
                    Archer archer = (Archer) actor;
                    if (archer.hasBleed()) {
                        for (Enemy e : enemies) {
                            if (!e.isDefeated()) {
                                int dmg = archer.tickBleed();
                                if (dmg > 0) {
                                    e.setHp(e.getHp() - dmg);
                                    say("Bleed! " + e.getName() + " takes " + dmg + " damage!");
                                    awardGoldForDefeated(enemies);
                                    pushScreenState(waveNum, enemies);
                                }
                            }
                        }
                    }
                }
            }

            if (allDefeated(enemies)) break;

            // Enemy turn
            say("Enemies are acting...");
            sleep(400);
            EnemyAI.executeEnemyTurns(enemies, partyManager.getParty(), tauntState);
            tauntState.tick();
            pushScreenState(waveNum, enemies);

            if (partyManager.isWiped()) return WaveOutcome.WIPED;

            say("Your turn — choose your action!");
        }

        return partyManager.isWiped() ? WaveOutcome.WIPED : WaveOutcome.SURVIVED;
    }

    // ── Reflect result in the dialog box ─────────────────────────────────────
    private void reflectResult(BattleTurnHandler.TurnResult result, Character actor) {
        switch (result) {
            case ATTACKED:    say(actor.getName() + " attacks!"); break;
            case USED_SKILL:  say(actor.getName() + " uses their skill!"); break;
            case USED_ITEM:   say(actor.getName() + " uses an item!"); break;
            case DEFENDED:    say(actor.getName() + " takes a defensive stance!"); break;
            case FLEE_FAILED: say("Couldn't flee! The party takes damage!"); break;
            case CANNOT_FLEE: say("You cannot flee from the boss!"); break;
            default: break;
        }
        sleep(500);
    }

    // ── Gold on kill ──────────────────────────────────────────────────────────
    private void awardGoldForDefeated(ArrayList<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.isDefeated() && !e.hasBeenRewarded()) {
                int gold = currency.awardForKill(e.getEnemyType());
                enemiesDefeated++;
                e.markRewarded();
                int totalGold = currency.getGold();
                SwingUtilities.invokeLater(() -> {
                    if (screen() != null) screen().setGold(totalGold);
                });
                say(e.getName() + " defeated!  +" + gold + "g  (Total: " + totalGold + "g)");
                sleep(300);
            }
        }
    }

    // ── Wave announcement (dialog text, no JOptionPane) ───────────────────────
    private void announceWave(int waveNum, ArrayList<Enemy> wave) {
        StringBuilder sb = new StringBuilder();
        if (waveNum == WaveManager.BOSS_WAVE) sb.append("⚠ BOSS WAVE — No escape!  ");
        else sb.append("Wave ").append(waveNum).append(" of ").append(WaveManager.TOTAL_WAVES).append("  —  ");
        sb.append("Enemies: ");
        for (int i = 0; i < wave.size(); i++) {
            sb.append(wave.get(i).getName());
            if (i < wave.size() - 1) sb.append(", ");
        }
        say(sb.toString());
        sleep(600);
        SwingUtilities.invokeLater(() -> {
            if (screen() != null) screen().setFleeEnabled(waveNum < WaveManager.BOSS_WAVE);
        });
    }

    // ── Between-wave town offer (JOptionPane — explicit menu choice) ──────────
    private void offerTownBetweenWaves(int justClearedWave) {
        int nextWave = justClearedWave + 1;
        String[] options = {
            "⚔  Proceed to Wave " + nextWave,
            "🏘  Visit Town  (💰 " + currency.getGold() + "g)"
        };
        int choice = JOptionPane.showOptionDialog(null,
                "Wave " + justClearedWave + " cleared! ✅\n\n"
                + partyManager.getPartyStatusBlock() + "\n"
                + "💰 Gold: " + currency.getGold() + "g",
                "Wave Clear!", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 1) TownScreen.open(partyManager, currency, nextWave);
        pushScreenState(nextWave, new ArrayList<>());
    }

    // ── End screen ────────────────────────────────────────────────────────────
    private void showEndScreen(boolean won) {
        SwingUtilities.invokeLater(() -> {
            GameWindow gw = GameWindow.getInstance();
            if (gw != null) gw.showEndScreen(won, enemiesDefeated, totalTurns, wavesCleared);
        });
    }

    private boolean allDefeated(ArrayList<Enemy> enemies) {
        for (Enemy e : enemies) if (!e.isDefeated()) return false;
        return true;
    }
}
