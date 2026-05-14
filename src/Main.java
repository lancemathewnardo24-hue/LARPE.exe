/**
 * LARP.exe: Break the Illusion
 * Authors: Ian / Lans
 *
 * Main.java
 * Entry point for the game.
 * Shows a title screen, collects the player's name, then launches BattleSystem.
 *
 * To compile:  javac -d out src/*.java
 * To run:      java -cp out Main
 */
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {

        // ── Title screen ──────────────────────────────────────────────────────
        JOptionPane.showMessageDialog(null,
                "██╗      █████╗ ██████╗ ██████╗      ███████╗██╗  ██╗███████╗\n" +
                "██║     ██╔══██╗██╔══██╗██╔══██╗     ██╔════╝╚██╗██╔╝██╔════╝\n" +
                "██║     ███████║██████╔╝██████╔╝     █████╗   ╚███╔╝ █████╗  \n" +
                "██║     ██╔══██║██╔══██╗██╔═══╝      ██╔══╝   ██╔██╗ ██╔══╝  \n" +
                "███████╗██║  ██║██║  ██║██║          ███████╗██╔╝ ██╗███████╗\n" +
                "╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝          ╚══════╝╚═╝  ╚═╝╚══════╝\n\n" +
                "         B R E A K   T H E   I L L U S I O N\n\n" +
                "A turn-based RPG where LARPers battle for convention glory.\n" +
                "Your party: Warrior  •  Mage  •  Archer\n" +
                "Survive 6 waves to reach the LARP Overlord.",
                "LARP.exe", JOptionPane.PLAIN_MESSAGE);

        // ── Name entry ────────────────────────────────────────────────────────
        String rawName = JOptionPane.showInputDialog(null,
                "Enter your hero name:", "Who dares enter?",
                JOptionPane.QUESTION_MESSAGE);

        // Player hit Cancel or closed — exit gracefully
        if (rawName == null) {
            JOptionPane.showMessageDialog(null,
                    "No hero, no glory. Goodbye.",
                    "LARP.exe", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String playerName = InputValidator.sanitizeName(rawName);

        // ── How-to-play ───────────────────────────────────────────────────────
        JOptionPane.showMessageDialog(null,
                "Welcome, " + playerName + "!\n\n" +
                "HOW TO PLAY\n" +
                "───────────\n" +
                "• Each round, every party member acts in order.\n" +
                "• Choose: Attack · Skill · Item · Defend · Flee\n" +
                "• Attacks can HIT, MISS (15%), or CRIT (20%).\n" +
                "• Flee has a 60% chance — failure damages your whole party.\n" +
                "• Enemies can Attack, Buff themselves, or Taunt you.\n" +
                "• Taunt forces your next action to be a weak Attack.\n" +
                "• Items drop after each wave — use them wisely.\n" +
                "• You CANNOT flee Wave 6 (Boss).\n\n" +
                "PARTY CLASSES\n" +
                "─────────────\n" +
                "Warrior  — 150 HP · Heavy Strike · Rage Slam (1.8–2.4×)\n" +
                "Mage     — 100 HP · Arcane Bolt  · Null Burst (2.5× | mana)\n" +
                "Archer   —  110 HP · Quick Shot  · Phantom Arrow (bleed)\n\n" +
                "Good luck!",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);

        // ── Launch battle ─────────────────────────────────────────────────────
        new BattleSystem(playerName).start();
    }
}
