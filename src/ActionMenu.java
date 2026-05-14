/**
 * LARP.exe: Break the Illusion
 *
 * ActionMenu.java
 * The bottom-right action panel — 4 pixel-art styled JButtons:
 *   ⚔ ATTACK  |  ✨ SKILL
 *   🎒 ITEM   |  🛡 DEFEND   (+ 🚪 FLEE in a 5th slot)
 *
 * Requirement: "All menus must show current HP, enemy HP, and available actions."
 * The HP is shown in BattleScreen; this panel provides the action buttons.
 *
 * Callbacks are delivered via ActionMenuListener so BattleScreen stays decoupled.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ActionMenu extends JPanel {

    public interface ActionMenuListener {
        void onAttack();
        void onSkill();
        void onItem();
        void onDefend();
        void onFlee();
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private ActionMenuListener listener;
    private JButton btnAttack, btnSkill, btnItem, btnDefend, btnFlee;
    private String skillName = "SKILL";

    // ── Constructor ───────────────────────────────────────────────────────────
    public ActionMenu() {
        setOpaque(false);
        setLayout(new GridLayout(3, 2, 6, 6));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        buildButtons();
    }

    private void buildButtons() {
        btnAttack = makeBtn("⚔  ATTACK",  Palette.BTN_BORDER);
        btnSkill  = makeBtn("✨  SKILL",   new Color(0x7755AA));
        btnItem   = makeBtn("🎒  ITEM",    new Color(0x3A6A3A));
        btnDefend = makeBtn("🛡  DEFEND",  new Color(0x4A5A7A));
        btnFlee   = makeBtn("🚪  FLEE",    new Color(0x5A3A3A));

        add(btnAttack);
        add(btnSkill);
        add(btnItem);
        add(btnDefend);
        add(btnFlee);
        add(new JLabel());  // empty cell to keep grid

        btnAttack.addActionListener(e -> { if (listener != null) listener.onAttack(); });
        btnSkill .addActionListener(e -> { if (listener != null) listener.onSkill();  });
        btnItem  .addActionListener(e -> { if (listener != null) listener.onItem();   });
        btnDefend.addActionListener(e -> { if (listener != null) listener.onDefend(); });
        btnFlee  .addActionListener(e -> { if (listener != null) listener.onFlee();   });
    }

    private JButton makeBtn(String text, Color borderColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                ButtonModel model = getModel();
                if (model.isPressed()) {
                    g2.setColor(Palette.BTN_PRESS);
                } else if (model.isRollover()) {
                    g2.setColor(Palette.BTN_HOVER);
                } else {
                    g2.setColor(Palette.BTN_NORMAL);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(model.isRollover() ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);

                // Shine
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(3, 3, getWidth()-6, getHeight()/2 - 3, 6, 6);

                // Text
                g2.setFont(GameFont.get(9f));
                g2.setColor(model.isEnabled() ? Palette.BTN_TEXT : Palette.TEXT_DIM);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(GameFont.get(9f));
        btn.setForeground(Palette.BTN_TEXT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 42));
        return btn;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setListener(ActionMenuListener l) { this.listener = l; }

    /** Enables or disables all action buttons (e.g. during animations). */
    public void setEnabled(boolean enabled) {
        btnAttack.setEnabled(enabled);
        btnSkill .setEnabled(enabled);
        btnItem  .setEnabled(enabled);
        btnDefend.setEnabled(enabled);
        btnFlee  .setEnabled(enabled);
    }

    /** Disables flee button on boss wave. */
    public void setFleeEnabled(boolean enabled) {
        btnFlee.setEnabled(enabled);
    }

    /** Updates the skill button label with the acting member's skill name. */
    public void updateSkillLabel(String name) {
        skillName = name;
        // Truncate long skill names for display
        String display = name.length() > 12 ? name.substring(0, 12) + "…" : name;
        btnSkill.setText("✨  " + display);
    }

    /** Highlights the attack button red when player is taunted. */
    public void setTaunted(boolean taunted) {
        btnAttack.setForeground(taunted ? Palette.TEXT_RED : Palette.BTN_TEXT);
        setEnabled(!taunted);           // lock other buttons
        btnAttack.setEnabled(true);     // keep attack available
    }
}
