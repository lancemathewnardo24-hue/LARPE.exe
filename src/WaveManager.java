/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * WaveManager.java
 * Defines and manages the 6 waves of enemies.
 * After each wave, distributes random item drops to the party Inventory.
 *
 * Wave layout (requirement: 1–3 enemies per wave, 6 types):
 *   Wave 1 — 1 Foam Knight         (tutorial)
 *   Wave 2 — 2 Shield Bearers      (defense check)
 *   Wave 3 — 1 Rogue LARPer + 1 Foam Knight  (speed)
 *   Wave 4 — 1 Dark Minstrel + 1 Shield Bearer (taunt + tank)
 *   Wave 5 — 2 Necro Cosplayers    (high damage)
 *   Wave 6 — 1 LARP Overlord       (BOSS — no flee)
 *
 * Item drops (requirement):
 *   Health Potion, Mana Elixir, or Revive Scroll — random per wave.
 *
 * NEW class.
 */
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JOptionPane;

public class WaveManager {

    public static final int TOTAL_WAVES = 6;
    public static final int BOSS_WAVE   = 6;   // matches InputValidator.validateFleeAttempt

    private int             currentWave = 0;
    private final Random    random      = new Random();

    // ── Wave construction ─────────────────────────────────────────────────────

    /**
     * Returns the list of Enemies for the next wave.
     * Increments the internal wave counter.
     *
     * @return ArrayList<Enemy> for the upcoming wave, or null if all waves done
     */
    public ArrayList<Enemy> nextWave() {
        currentWave++;
        if (currentWave > TOTAL_WAVES) return null;
        return buildWave(currentWave);
    }

    /** Returns enemies for the given wave without incrementing the counter. */
    public ArrayList<Enemy> peekWave(int wave) {
        return buildWave(wave);
    }

    private ArrayList<Enemy> buildWave(int wave) {
        ArrayList<Enemy> enemies = new ArrayList<>();
        switch (wave) {
            case 1:
                enemies.add(Enemy.create(Enemy.EnemyType.FOAM_KNIGHT));
                break;
            case 2:
                enemies.add(Enemy.create(Enemy.EnemyType.SHIELD_BEARER));
                enemies.add(Enemy.create(Enemy.EnemyType.SHIELD_BEARER));
                break;
            case 3:
                enemies.add(Enemy.create(Enemy.EnemyType.ROGUE_LARPER));
                enemies.add(Enemy.create(Enemy.EnemyType.FOAM_KNIGHT));
                break;
            case 4:
                enemies.add(Enemy.create(Enemy.EnemyType.DARK_MINSTREL));
                enemies.add(Enemy.create(Enemy.EnemyType.SHIELD_BEARER));
                break;
            case 5:
                enemies.add(Enemy.create(Enemy.EnemyType.NECRO_COSPLAYER));
                enemies.add(Enemy.create(Enemy.EnemyType.NECRO_COSPLAYER));
                break;
            case 6:
                enemies.add(Enemy.create(Enemy.EnemyType.LARP_OVERLORD));
                break;
        }
        return enemies;
    }

    // ── Item drops ────────────────────────────────────────────────────────────

    /**
     * Awards 1–2 random item drops to the party's inventory after a wave.
     * Called by BattleSystem immediately after wave victory.
     *
     * @param inventory the party's shared Inventory to add drops to
     * @param wave      the wave number just cleared (for drop count scaling)
     */
    public void grantDrops(Inventory inventory, int wave) {
        // Boss wave always drops 2 items; others drop 1–2 randomly
        int dropCount = (wave == BOSS_WAVE) ? 2 : (random.nextBoolean() ? 2 : 1);

        StringBuilder dropLog = new StringBuilder("🎁 ITEM DROP(S):\n");
        for (int i = 0; i < dropCount; i++) {
            Item drop = generateRandomDrop();
            boolean added = inventory.addItem(drop);
            if (added) {
                dropLog.append("  + ").append(drop.getName()).append("\n");
            } else {
                dropLog.append("  ✗ Inventory full — ").append(drop.getName())
                       .append(" lost.\n");
            }
        }

        JOptionPane.showMessageDialog(null,
                dropLog.toString().trim(),
                "Wave " + wave + " Clear!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Returns a random Item from the three drop types. */
    private Item generateRandomDrop() {
        int roll = random.nextInt(3);
        switch (roll) {
            case 0:
                return new Item("Health Potion",
                        "Restores 50 HP",
                        Item.ItemEffect.HEAL, 50,
                        "assets/images/item_health_potion.png");
            case 1:
                return new Item("Mana Elixir",
                        "Restores 40 MP (or 20 HP for non-mages)",
                        Item.ItemEffect.RESTORE_MANA, 40,
                        "assets/images/item_mana_elixir.png");
            default:
                return new Item("Revive Scroll",
                        "Revives at 40% HP, or heals 60 HP if alive",
                        Item.ItemEffect.REVIVE, 60,
                        "assets/images/item_revive_scroll.png");
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int  getCurrentWave()  { return currentWave;              }
    public boolean isBossWave()   { return currentWave == BOSS_WAVE; }
    public boolean hasMoreWaves() { return currentWave < TOTAL_WAVES; }
}
