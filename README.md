# LARP.exe: Break the Illusion

A turn-based RPG battle system built in Java with JOptionPane UI.

---

## 📁 Folder Structure

```
LARPexe/
├── src/                        ← All .java source files go here
│   ├── Main.java               ← Entry point (START HERE)
│   │
│   ├── — Core (unchanged) —
│   ├── Character.java          ← Abstract base class
│   ├── Warrior.java            ← Heavy brawler subclass
│   ├── Mage.java               ← Glass cannon subclass
│   ├── Archer.java             ← Bleed/dodge subclass
│   ├── Inventory.java          ← ArrayList<Item> manager
│   ├── Item.java               ← Consumable items
│   ├── BattleException.java    ← Custom battle exception
│   ├── EmptyInventoryException.java
│   ├── InputValidator.java     ← All input validation (JOptionPane)
│   │
│   ├── — New / Upgraded —
│   ├── BattleSystem.java       ← Main game loop (wave orchestrator)
│   ├── BattleTurnHandler.java  ← Processes one party member's turn
│   ├── PartyManager.java       ← Manages 3-character party
│   ├── Enemy.java              ← Enemy Character subclass (6 types)
│   ├── EnemyAI.java            ← Enemy turn logic (Attack/Buff/Taunt)
│   ├── WaveManager.java        ← 6 wave definitions + item drops
│   ├── CombatResolver.java     ← Hit / Miss / Crit engine
│   ├── CombatResult.java       ← Value object for combat outcomes
│   └── TauntState.java         ← Taunt mechanic tracker
│
├── assets/
│   ├── images/                 ← Item/character PNGs (Phase 5 GUI)
│   │   ├── item_health_potion.png
│   │   ├── item_mana_elixir.png
│   │   └── item_revive_scroll.png
│   └── audio/                  ← Sound effects (future phase)
│
├── out/                        ← Compiled .class files land here
└── README.md
```

---

## ▶️ How to Run

### Prerequisites
- Java JDK 11 or later
- Check with: `java -version` and `javac -version`
- Download from: https://adoptium.net (free)

### Step 1 — Compile
Open a terminal in the `LARPexe/` folder and run:
```bash
javac -d out src/*.java
```
All `.class` files will be placed in the `out/` folder.

### Step 2 — Run
```bash
java -cp out Main
```

### One-liner (compile + run)
```bash
javac -d out src/*.java && java -cp out Main
```

---

## 🎮 Gameplay Summary

| Feature | Detail |
|---|---|
| Party size | 3 members (Warrior, Mage, Archer) |
| Waves | 6 total — Wave 6 is the Boss (no flee) |
| Enemies per wave | 1–3 |
| Attack roll | 15% miss · 65% hit · 20% crit (1.75×) |
| Flee chance | 60% success — failure damages entire party (15% maxHP) |
| Taunt | 40% success — forces Basic Attack with 50% miss next turn |
| Item drops | 1–2 random items after each wave |
| Enemy AI | Random weighted: Attack / Buff / Taunt |

---

## 🩹 Troubleshooting

**"javac not found"** — JDK not installed or not on PATH. Install from adoptium.net.

**"cannot find symbol" compile errors** — Make sure ALL `.java` files are in `src/` before compiling.

**JOptionPane dialogs don't appear on macOS** — Run from Terminal (not Finder). Should work fine.

**Black screen / nothing happens** — Some IDEs block Swing on the wrong thread. Run from the terminal using the commands above instead.
