/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * BattleScreen.java  ← FULL POKEMON EMERALD LAYOUT
 *
 * Exact layout (900 × 620):
 *
 *  ┌──────────────────────────────────────────────────────────────┐
 *  │  WAVE X/6                                    💰 XXXg  [TOP] │  ← 36px header
 *  ├──────────────────────────────────────────────────────────────┤
 *  │                                                              │
 *  │   ENEMY stat box (top-left)    ENEMY SPRITE (top-right)     │  ← battle field
 *  │   [Name]  HP ████░░░           (large, facing left)         │    ~355px tall
 *  │   [HP 80/120]                                               │
 *  │                                                              │
 *  │   PARTY SPRITE (bottom-left)   PARTY stat box (bottom-right)│
 *  │   (large, facing right)        [Name] HP ████░░░            │
 *  │                                [HP / MP bars]               │
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  ┌─────────────────────────────┐  ┌───────┐  ┌───────┐     │  ← 229px bottom
 *  │  │ DIALOG BOX                  │  │ATTACK │  │ SKILL │     │
 *  │  │ "What will WARRIOR do?"     │  ├───────┤  ├───────┤     │
 *  │  │                             │  │ ITEM  │  │DEFEND │     │
 *  │  └─────────────────────────────┘  └───────┘  └───────┘     │
 *  │                                        [FLEE - full width]  │
 *  └──────────────────────────────────────────────────────────────┘
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BattleScreen extends JPanel implements ActionListener {

    // ── Layout constants (Pokémon Emerald proportions) ────────────────────────
    private static final int W          = 900;
    private static final int H          = 620;
    private static final int HEADER_H   = 36;
    private static final int BOTTOM_H   = 229;
    private static final int FIELD_H    = H - HEADER_H - BOTTOM_H;  // 355px

    // Field split: enemy side upper-right, party side lower-left
    private static final int FIELD_Y    = HEADER_H;                  // 36
    private static final int BOTTOM_Y   = HEADER_H + FIELD_H;        // 391

    // ── Sprite state ──────────────────────────────────────────────────────────
    public enum WarriorState { IDLE, ATTACK, HURT }
    private WarriorState warriorState    = WarriorState.IDLE;
    private int          stateFrames     = 0;
    private static final int ATK_FRAMES  = 18;
    private static final int HURT_FRAMES = 14;

    // ── Active party member indicator (for "Who acts?" arrow) ────────────────
    private int activePartySlot = 0;   // 0=Warrior, 1=Mage, 2=Archer

    // ── Gold tracker ─────────────────────────────────────────────────────────
    private int displayedGold = 0;

    // ── Core refs ─────────────────────────────────────────────────────────────
    private final GameWindow      window;
    private final AnimationEngine anim = new AnimationEngine();
    private PartyManager          partyManager;
    private ArrayList<Enemy>      currentEnemies = new ArrayList<>();
    private int                   currentWave    = 1;
    private String                dialogText     = "Choose your action.";

    // ── Sprites ───────────────────────────────────────────────────────────────
    private BufferedImage sprWarriorIdle, sprWarriorAttack, sprWarriorHurt;

    // ── Bottom panel Swing components ─────────────────────────────────────────
    private JButton btnAttack, btnSkill, btnItem, btnDefend, btnFlee;
    private JPanel  bottomPanel;

    // ── Render timer ──────────────────────────────────────────────────────────
    private final Timer renderTimer;

    // ── Listener interface ────────────────────────────────────────────────────
    public interface ActionListener2 {
        void onAttack(); void onSkill(); void onItem();
        void onDefend(); void onFlee();
    }
    private ActionListener2 actionListener;
    public void setActionListener(ActionListener2 l) { this.actionListener = l; }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════════
    public BattleScreen(GameWindow window) {
        this.window = window;
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(0x10121E));
        setLayout(null);

        sprWarriorIdle   = SpriteLoader.get(SpriteLoader.WARRIOR_IDLE);
        sprWarriorAttack = SpriteLoader.get(SpriteLoader.WARRIOR_ATTACK);
        sprWarriorHurt   = SpriteLoader.get(SpriteLoader.WARRIOR_HURT);

        buildBottomPanel();

        renderTimer = new Timer(60, this);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BOTTOM PANEL  —  Dialog box (left) + 2×2 grid + Flee (right)
    // ═══════════════════════════════════════════════════════════════════════════
    private void buildBottomPanel() {
        bottomPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // White background exactly like Pokémon Emerald bottom panel
                g2.setColor(new Color(0xF0F0E8));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Thin top border line (dark)
                g2.setColor(new Color(0x202030));
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        bottomPanel.setOpaque(true);
        bottomPanel.setBounds(0, BOTTOM_Y, W, BOTTOM_H);
        add(bottomPanel);

        // ── Dialog area (left, ~560px wide) ──────────────────────────────────
        // Painted directly in paintComponent via drawDialogBox() — nothing to add here

        // ── Action buttons (right side, 2×2 + flee row) ──────────────────────
        int btnX  = 570;   // right block starts here
        int btnW  = 155;
        int btnH  = 58;
        int gap   = 8;
        int row1Y = 18;
        int row2Y = row1Y + btnH + gap;
        int row3Y = row2Y + btnH + gap;

        btnAttack = makePkmBtn("ATTACK",  new Color(0xE83030), new Color(0xFF6060));
        btnSkill  = makePkmBtn("SKILL",   new Color(0x3060D0), new Color(0x60A0FF));
        btnItem   = makePkmBtn("ITEM",    new Color(0x30A030), new Color(0x60E060));
        btnDefend = makePkmBtn("DEFEND",  new Color(0xB07820), new Color(0xF0C040));
        btnFlee   = makePkmBtn("FLEE",    new Color(0x606060), new Color(0xA0A0A0));

        btnAttack.setBounds(btnX,            row1Y, btnW, btnH);
        btnSkill .setBounds(btnX + btnW + gap, row1Y, btnW, btnH);
        btnItem  .setBounds(btnX,            row2Y, btnW, btnH);
        btnDefend.setBounds(btnX + btnW + gap, row2Y, btnW, btnH);
        // Flee spans full width on row 3
        btnFlee  .setBounds(btnX, row3Y, btnW * 2 + gap, 40);

        bottomPanel.add(btnAttack);
        bottomPanel.add(btnSkill);
        bottomPanel.add(btnItem);
        bottomPanel.add(btnDefend);
        bottomPanel.add(btnFlee);

        btnAttack.addActionListener(e -> { if (actionListener != null) actionListener.onAttack(); });
        btnSkill .addActionListener(e -> { if (actionListener != null) actionListener.onSkill();  });
        btnItem  .addActionListener(e -> { if (actionListener != null) actionListener.onItem();   });
        btnDefend.addActionListener(e -> { if (actionListener != null) actionListener.onDefend(); });
        btnFlee  .addActionListener(e -> { if (actionListener != null) actionListener.onFlee();   });
    }

    /** Creates a Pokémon-style action button with a two-tone colour scheme. */
    private JButton makePkmBtn(String label, Color dark, Color light) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                ButtonModel m = getModel();

                // Body
                Color base = m.isPressed() ? dark.darker() : m.isRollover() ? light : dark;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                // Highlight strip (top third)
                if (!m.isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 55));
                    g2.fillRoundRect(3, 3, w - 6, h / 3, 7, 7);
                }

                // Outer border
                g2.setColor(dark.darker().darker());
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 10, 10);

                // Label — pixel font, white with dark shadow
                g2.setFont(GameFont.get(10f));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(0, 0, 0, 120));
                g2.drawString(getText(), tx + 1, ty + 1);
                g2.setColor(m.isEnabled() ? Color.WHITE : new Color(0xCCCCCC));
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(GameFont.get(10f));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════════════════
    public void init(String playerName) {
        partyManager   = new PartyManager(playerName);
        currentWave    = 1;
        dialogText     = "Wave 1  —  Choose your action!";
        warriorState   = WarriorState.IDLE;
        activePartySlot = 0;
        currentEnemies.clear();
        setButtonsEnabled(true);
        renderTimer.start();
        new Thread(() -> new BattleSystem(playerName).start()).start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RENDER TICK
    // ═══════════════════════════════════════════════════════════════════════════
    @Override public void actionPerformed(ActionEvent e) {
        anim.update();
        if (stateFrames > 0 && --stateFrames == 0) warriorState = WarriorState.IDLE;
        repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PAINT
    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        anim.applyShake(g2);

        drawHeader(g2);
        drawBattleField(g2);
        drawDialogBox(g2);
        anim.drawDamageNumbers(g2);

        g2.dispose();
    }

    // ── 1. HEADER BAR ─────────────────────────────────────────────────────────
    private void drawHeader(Graphics2D g2) {
        // Dark strip
        g2.setColor(new Color(0x08090F));
        g2.fillRect(0, 0, W, HEADER_H);
        g2.setColor(new Color(0xC9A84C));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(0, HEADER_H - 1, W, HEADER_H - 1);

        // Wave label
        g2.setFont(GameFont.get(9f));
        g2.setColor(new Color(0xC9A84C));
        g2.drawString("WAVE  " + currentWave + " / 6", 14, 22);

        // Boss warning
        if (currentWave == 6) {
            g2.setColor(new Color(0xE05555));
            g2.drawString("⚠ BOSS  —  NO FLEE", W / 2 - 90, 22);
        }

        // Gold
        g2.setColor(new Color(0xECEFF8));
        String goldTxt = "GOLD  " + displayedGold + "g";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(goldTxt, W - fm.stringWidth(goldTxt) - 14, 22);
    }

    // ── 2. BATTLE FIELD ───────────────────────────────────────────────────────
    private void drawBattleField(Graphics2D g2) {
        // Background: sky gradient
        GradientPaint sky = new GradientPaint(0, FIELD_Y,
                new Color(0x8BBFE8), 0, FIELD_Y + FIELD_H * 0.55f, new Color(0xC5E0F5));
        g2.setPaint(sky);
        g2.fillRect(0, FIELD_Y, W, (int)(FIELD_H * 0.55f));

        // Ground: two platforms (Emerald style)
        // Enemy platform (upper-right)
        drawPlatform(g2, W / 2 - 30, FIELD_Y + 60, 380, 55);
        // Party platform (lower-left)
        drawPlatform(g2, -30, FIELD_Y + 175, 380, 55);

        // Lower ground strip
        GradientPaint ground = new GradientPaint(0, FIELD_Y + (int)(FIELD_H * 0.55f),
                new Color(0x7BAF5A), 0, FIELD_Y + FIELD_H, new Color(0x4A7A38));
        g2.setPaint(ground);
        g2.fillRect(0, FIELD_Y + (int)(FIELD_H * 0.55f), W, (int)(FIELD_H * 0.45f));

        // Enemy stat box  (top-LEFT, Pokémon style)
        drawEnemyStatBox(g2);

        // Party stat box  (bottom-RIGHT)
        drawPartyStatBox(g2);

        // Enemy sprite(s) on their platform (top-right)
        drawEnemySprites(g2);

        // Party sprites on their platform (bottom-left)
        drawPartySprites(g2);
    }

    /** Draws an elliptical grass platform like Pokémon Emerald. */
    private void drawPlatform(Graphics2D g2, int x, int y, int pw, int ph) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(x + 6, y + 6, pw, ph);
        // Platform fill
        GradientPaint pg = new GradientPaint(x, y, new Color(0x8EC56A),
                x, y + ph, new Color(0x5A9040));
        g2.setPaint(pg);
        g2.fillOval(x, y, pw, ph);
        // Highlight rim
        g2.setColor(new Color(0xA8D87A));
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(x + 10, y + 4, pw - 20, ph / 2, 10, 160);
    }

    // ── ENEMY STAT BOX (top-left, Emerald style white box) ────────────────────
    private void drawEnemyStatBox(Graphics2D g2) {
        if (currentEnemies.isEmpty()) return;
        Enemy first = null;
        for (Enemy e : currentEnemies) if (!e.isDefeated()) { first = e; break; }
        if (first == null) return;

        int bx = 14, by = FIELD_Y + 14, bw = 260, bh = 68;
        drawStatBox(g2, bx, by, bw, bh);

        // Enemy name
        g2.setFont(GameFont.get(9f));
        g2.setColor(new Color(0x202030));
        String name = first.getName();
        g2.drawString(name, bx + 10, by + 18);

        // Level / buffed tag
        g2.setFont(GameFont.get(7f));
        g2.setColor(first.isBuffed() ? new Color(0x3060D0) : new Color(0x707090));
        g2.drawString(first.isBuffed() ? "Lv.? ★BUFFED" : "Lv.?", bx + bw - 80, by + 18);

        // HP label
        g2.setFont(GameFont.get(7f));
        g2.setColor(new Color(0x404050));
        g2.drawString("HP", bx + 10, by + 36);

        // HP bar
        HpBar.draw(g2, bx + 30, by + 26, bw - 44, 11, first.getHp(), first.getMaxHp(), false);

        // HP numbers (enemy box in Emerald doesn't show numbers — but we will)
        g2.setFont(GameFont.get(7f));
        g2.setColor(new Color(0x404050));
        g2.drawString(first.getHp() + " / " + first.getMaxHp(), bx + bw - 78, by + 54);

        // If multiple enemies, show small secondary name
        int alive = 0;
        for (Enemy e : currentEnemies) if (!e.isDefeated()) alive++;
        if (alive > 1) {
            g2.setFont(GameFont.get(7f));
            g2.setColor(new Color(0xE05555));
            g2.drawString("+" + (alive - 1) + " more", bx + 10, by + 54);
        }
    }

    // ── PARTY STAT BOX (bottom-right, shows all 3 members) ───────────────────
    private void drawPartyStatBox(Graphics2D g2) {
        if (partyManager == null) return;
        ArrayList<Character> party = partyManager.getParty();

        int bx = W - 290, by = FIELD_Y + FIELD_H - 160, bw = 278, bh = 148;
        drawStatBox(g2, bx, by, bw, bh);

        int rowH = 44;
        for (int i = 0; i < party.size(); i++) {
            Character c = party.get(i);
            int ry = by + 8 + i * rowH;

            // Active member arrow (▶)
            if (i == activePartySlot && !c.isDefeated()) {
                g2.setFont(GameFont.get(9f));
                g2.setColor(new Color(0xE83030));
                g2.drawString("▶", bx + 4, ry + 14);
            }

            // Name
            g2.setFont(GameFont.get(8f));
            g2.setColor(c.isDefeated() ? new Color(0xAAAAAA) : new Color(0x202030));
            String shortName = c.getName().contains("[") 
                ? c.getName().substring(c.getName().indexOf("[") + 1, c.getName().indexOf("]"))
                : c.getName();
            g2.drawString(shortName, bx + 18, ry + 14);

            // HP bar
            g2.setFont(GameFont.get(7f));
            g2.setColor(new Color(0x505060));
            g2.drawString("HP", bx + 18, ry + 28);
            HpBar.draw(g2, bx + 38, ry + 19, 130, 10,
                    c.isDefeated() ? 0 : c.getHp(), c.getMaxHp(), false);

            // HP numbers
            g2.setFont(GameFont.get(7f));
            g2.setColor(new Color(0x404050));
            String hpStr = c.isDefeated() ? "  --/--" : c.getHp() + "/" + c.getMaxHp();
            g2.drawString(hpStr, bx + 174, ry + 28);

            // MP bar for Mage
            if (c instanceof Mage) {
                Mage m = (Mage) c;
                g2.setFont(GameFont.get(7f));
                g2.setColor(new Color(0x3060D0));
                g2.drawString("MP", bx + 18, ry + 40);
                HpBar.draw(g2, bx + 38, ry + 31, 90, 7, m.getMana(), m.getMaxMana(), true);
            }

            // Fainted label
            if (c.isDefeated()) {
                g2.setFont(GameFont.get(7f));
                g2.setColor(new Color(0xE05555));
                g2.drawString("FAINTED", bx + 200, ry + 14);
            }

            // Row divider
            if (i < party.size() - 1) {
                g2.setColor(new Color(0xC8C8C0));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(bx + 8, ry + rowH - 2, bx + bw - 8, ry + rowH - 2);
            }
        }
    }

    /** White rounded box with shadow — Pokémon Emerald stat box style. */
    private void drawStatBox(Graphics2D g2, int x, int y, int w, int h) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRoundRect(x + 4, y + 4, w, h, 14, 14);
        // Box
        g2.setColor(new Color(0xF4F4EC));
        g2.fillRoundRect(x, y, w, h, 14, 14);
        // Border
        g2.setColor(new Color(0x787878));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 14, 14);
        // Inner highlight
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x + 3, y + 3, w - 6, h - 6, 11, 11);
    }

    // ── ENEMY SPRITES ─────────────────────────────────────────────────────────
    private void drawEnemySprites(Graphics2D g2) {
        if (currentEnemies.isEmpty()) {
            drawPlaceholderSprite(g2, W - 230, FIELD_Y + 55, 160, 160, "ENEMY");
            return;
        }

        int alive = 0;
        for (Enemy e : currentEnemies) if (!e.isDefeated()) alive++;
        int spacing = alive > 1 ? 150 : 0;
        int startX  = W - 80 - alive * 140;

        int slot = 0;
        for (Enemy enemy : currentEnemies) {
            if (enemy.isDefeated()) continue;

            int ex = startX + slot * spacing;
            int ey = FIELD_Y + 40;
            int sw = 160, sh = 180;

            BufferedImage spr = SpriteLoader.get(
                "sprites/" + enemy.getEnemyType().name().toLowerCase() + ".png");

            if (anim.isFlashing("enemy_" + slot)) {
                g2.drawImage(makeFlash(spr, sw, sh), ex, ey, sw, sh, null);
            } else {
                g2.drawImage(spr, ex, ey, sw, sh, null);
            }

            // Buff shimmer border
            if (enemy.isBuffed()) {
                g2.setColor(new Color(0x4488FF, false));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(ex, ey, sw, sh, 10, 10);
            }

            slot++;
        }
    }

    // ── PARTY SPRITES ─────────────────────────────────────────────────────────
    private void drawPartySprites(Graphics2D g2) {
        if (partyManager == null) return;
        ArrayList<Character> party = partyManager.getParty();

        // In Pokémon Emerald only the active member's back sprite shows.
        // We show all three side-by-side, smaller, with the active one larger.
        int baseX = 30;
        int baseY = FIELD_Y + 130;

        for (int i = 0; i < party.size(); i++) {
            Character c = party.get(i);
            boolean active  = (i == activePartySlot);
            boolean fainted = c.isDefeated();

            int sw = active ? 130 : 80;
            int sh = active ? 150 : 95;
            int px = baseX + i * 100 + (active ? -10 : 0);
            int py = baseY + (active ? 0 : 30);

            BufferedImage spr = getSpriteFor(c);

            if (fainted) {
                // Fainted: draw sideways tint (rotated + grey)
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
                g2.drawImage(spr, px, py, sw, sh, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            } else if (anim.isFlashing("party_" + i)) {
                g2.drawImage(makeFlash(spr, sw, sh), px, py, sw, sh, null);
            } else {
                g2.drawImage(spr, px, py, sw, sh, null);
            }

            // Active member glow ring
            if (active && !fainted) {
                g2.setColor(new Color(0xFFD700, false));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(px + sw/2 - 36, py + sh - 18, 72, 20);
            }

            // Class label under sprite
            g2.setFont(GameFont.get(7f));
            g2.setColor(fainted ? new Color(0x999999) : new Color(0x202030));
            String lbl = fainted ? "✝" : (active ? "▶ " : "  ") + getClassName(c);
            g2.drawString(lbl, px + 2, py + sh + 13);
        }
    }

    // ── 3. DIALOG BOX ─────────────────────────────────────────────────────────
    private void drawDialogBox(Graphics2D g2) {
        int bx = 0, by = BOTTOM_Y, bw = 555, bh = BOTTOM_H;

        // White Pokémon-style dialog background
        g2.setColor(new Color(0xF0F0E8));
        g2.fillRect(bx, by, bw, bh);

        // Left + bottom + top borders (no right — buttons panel continues)
        g2.setColor(new Color(0x202030));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(bx, by, bx + bw, by);           // top
        g2.drawLine(bx, by, bx, by + bh);            // left
        g2.drawLine(bx, by + bh - 1, bx + bw, by + bh - 1);  // bottom

        // Vertical divider between dialog and buttons
        g2.drawLine(bw, by, bw, by + bh);

        // Dialog text
        g2.setFont(GameFont.get(11f));
        g2.setColor(new Color(0x101020));
        drawWrappedText(g2, dialogText, bx + 18, by + 38, bw - 36, 22);

        // Bottom-right triangle cursor (blinking would need tick logic)
        g2.setColor(new Color(0x202030));
        int tx = bx + bw - 22, ty = by + bh - 22;
        g2.fillPolygon(new int[]{tx, tx + 12, tx + 6}, new int[]{ty, ty, ty + 10}, 3);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private BufferedImage getSpriteFor(Character c) {
        if (c instanceof Warrior) {
            switch (warriorState) {
                case ATTACK: return sprWarriorAttack;
                case HURT:   return sprWarriorHurt;
                default:     return sprWarriorIdle;
            }
        }
        if (c instanceof Mage)   return SpriteLoader.get(SpriteLoader.MAGE);
        if (c instanceof Archer) return SpriteLoader.get(SpriteLoader.ARCHER);
        return sprWarriorIdle;
    }

    private String getClassName(Character c) {
        if (c instanceof Warrior) return "WARRIOR";
        if (c instanceof Mage)    return "MAGE";
        if (c instanceof Archer)  return "ARCHER";
        return "???";
    }

    private void drawPlaceholderSprite(Graphics2D g2, int x, int y, int w, int h, String lbl) {
        g2.setColor(new Color(0x2A2A3A, false));
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setFont(GameFont.get(8f));
        g2.setColor(new Color(0x8A94B0));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(lbl, x + (w - fm.stringWidth(lbl)) / 2, y + h / 2 + 4);
    }

    private BufferedImage makeFlash(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = out.createGraphics();
        fg.drawImage(src, 0, 0, w, h, null);
        fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.70f));
        fg.setColor(Color.WHITE);
        fg.fillRect(0, 0, w, h);
        fg.dispose();
        return out;
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y,
                                  int maxW, int lineH) {
        if (text == null || text.isEmpty()) return;
        FontMetrics fm = g2.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int cy = y;
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && line.length() > 0) {
                g2.drawString(line.toString(), x, cy);
                cy += lineH;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, cy);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC API — called by BattleSystem / BattleTurnHandler
    // ═══════════════════════════════════════════════════════════════════════════
    public void setDialogText(String t)              { dialogText = t;           repaint(); }
    public void setCurrentWave(int w)                { currentWave = w;          repaint(); }
    public void setCurrentEnemies(ArrayList<Enemy> e){ currentEnemies = e;       repaint(); }
    public void setPartyManager(PartyManager pm)     { partyManager = pm;        repaint(); }
    public void setActiveSlot(int slot)              { activePartySlot = slot;   repaint(); }
    public void setGold(int gold)                    { displayedGold = gold;     repaint(); }

    public void playWarriorAttack() {
        warriorState = WarriorState.ATTACK; stateFrames = ATK_FRAMES;
        anim.flashSprite("party_0", 8);
    }
    public void playWarriorHurt() {
        warriorState = WarriorState.HURT; stateFrames = HURT_FRAMES;
        anim.flashSprite("party_0", 6);
    }
    public void flashEnemy(int slot, int dmg, boolean crit) {
        anim.flashSprite("enemy_" + slot, crit ? 14 : 8);
        if (crit) anim.shakeScreen(10);
        int ex = W - 80 - currentEnemies.size() * 140 + slot * 150 + 70;
        anim.showDamageNumber((crit ? "⚡ " : "") + "-" + dmg, ex, FIELD_Y + 80,
                crit ? new Color(0xFFDD44) : new Color(0xECEFF8));
    }
    public void showHeal(int slot, int amount) {
        int px = 30 + slot * 100;
        anim.showDamageNumber("+" + amount + " HP", px + 20, FIELD_Y + 160,
                new Color(0x5EC95E));
    }

    public void setButtonsEnabled(boolean on) {
        btnAttack.setEnabled(on); btnSkill.setEnabled(on);
        btnItem.setEnabled(on);   btnDefend.setEnabled(on);
        btnFlee.setEnabled(on);
    }
    public void setFleeEnabled(boolean on)   { btnFlee.setEnabled(on); }
    public void setSkillLabel(String name) {
        String s = name.length() > 10 ? name.substring(0, 10) + "…" : name;
        btnSkill.setText(s);
        btnSkill.repaint();
    }
}
