package service;

import model.*;
import java.util.*;

/**
 * Turn-Based Combat Simulator
 * Section 10.1 Extra Credit
 *
 * Rules:
 * - Both sides attack alternately until one HP <= 0
 * - Damage formula: max(1, attackerTotalATK - defenderTotalDEF*0.6) + random fluctuation(-5~+5)
 * - Critical hit rate 15% (+5% if attacker has attack equipment) -> damage x 1.5
 * - Dodge rate 10% (+5% if defender has defense equipment)
 * - Returns full combat log as String
 */
public class CombatSimulator {

    private static final Random RNG = new Random();

    /**
     * Simulate a battle between two heroes (with their respective equipment).
     * Returns the combat log as a formatted String.
     */
    public static String simulate(GameData data, String heroName1, String heroName2) {
        if (data == null || data.getHeroes() == null) return "Error: no data available.";

        Hero h1 = findHero(data, heroName1);
        Hero h2 = findHero(data, heroName2);
        if (h1 == null || h2 == null) return "Hero not found";

        int atk1 = h1.getAtk(), def1 = h1.getDef(), hp1 = h1.getHp();
        int atk2 = h2.getAtk(), def2 = h2.getDef(), hp2 = h2.getHp();
        boolean hasAtkEq1 = false, hasDefEq1 = false;
        boolean hasAtkEq2 = false, hasDefEq2 = false;

        for (Equipment e : h1.getCompatibleEquipments()) {
            atk1 += e.getBonusAtk(); def1 += e.getBonusDef(); hp1 += e.getBonusHp();
            if (e.getBonusAtk() > 0) hasAtkEq1 = true;
            if (e.getBonusDef() > 0) hasDefEq1 = true;
        }
        for (Equipment e : h2.getCompatibleEquipments()) {
            atk2 += e.getBonusAtk(); def2 += e.getBonusDef(); hp2 += e.getBonusHp();
            if (e.getBonusAtk() > 0) hasAtkEq2 = true;
            if (e.getBonusDef() > 0) hasDefEq2 = true;
        }

        int maxHp1 = hp1, maxHp2 = hp2;
        int turn = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Combat Simulator ==========\n");
        sb.append(String.format("%-20s HP=%-6d ATK=%-4d DEF=%-4d [%s]\n",
                h1.getName(), hp1, atk1, def1, h1.getHeroRole()));
        sb.append(String.format("%-20s HP=%-6d ATK=%-4d DEF=%-4d [%s]\n",
                h2.getName(), hp2, atk2, def2, h2.getHeroRole()));
        sb.append("-----------------------------\n");

        while (hp1 > 0 && hp2 > 0 && turn < 500) {
            turn++;
            sb.append("--- Round ").append(turn).append(" ---\n");
            hp2 -= attack(sb, "  " + h1.getName(), atk1, def2, hasAtkEq1, hasDefEq2);
            if (hp2 <= 0) { sb.append("  ").append(h2.getName()).append(" defeated!\n"); break; }
            hp1 -= attack(sb, "  " + h2.getName(), atk2, def1, hasAtkEq2, hasDefEq1);
            if (hp1 <= 0) { sb.append("  ").append(h1.getName()).append(" defeated!\n"); break; }
        }

        sb.append("-----------------------------\n");
        if (hp1 > 0 && hp2 > 0) {
            sb.append("Result: DRAW! Both survived 500 rounds.\n");
            sb.append("HP: ").append(h1.getName()).append("=").append(hp1).append("/").append(maxHp1)
              .append(", ").append(h2.getName()).append("=").append(hp2).append("/").append(maxHp2).append("\n");
        } else {
            String winner = hp1 > 0 ? h1.getName() : h2.getName();
            int winnerRemain = hp1 > 0 ? hp1 : hp2;
            int winnerMax = hp1 > 0 ? maxHp1 : maxHp2;
            sb.append("Result: ").append(winner).append(" wins!\n");
            sb.append("HP Remaining: ").append(winnerRemain).append("/").append(winnerMax).append("\n");
        }
        sb.append("Total Rounds: ").append(turn).append("\n");
        sb.append("==============================\n");
        return sb.toString();
    }

    /**
     * Deterministic damage calculation for testing.
     * Pass a seeded Random to get reproducible results.
     * Returns the damage dealt.
     */
    public static int calculateDamage(int atk, int targetDef, boolean hasAtkEq, boolean hasDefEq, Random rng) {
        int baseDmg = Math.max(1, atk - (int)(targetDef * 0.6));
        int dmg = baseDmg + rng.nextInt(11) - 5;

        int critChance = hasAtkEq ? 20 : 15;
        if (rng.nextInt(100) < critChance) dmg = (int)(dmg * 1.5);

        int dodgeChance = hasDefEq ? 15 : 10;
        if (rng.nextInt(100) < dodgeChance) return 0;

        return Math.max(1, dmg);
    }

    private static int attack(StringBuilder sb, String attacker, int atk, int targetDef,
                               boolean hasAtkEq, boolean hasDefEq) {
        int dmg = calculateDamage(atk, targetDef, hasAtkEq, hasDefEq, RNG);
        if (dmg == 0) sb.append(attacker).append(" attacks -> dodged! (0)\n");
        else sb.append(attacker).append(" attacks -> ").append(dmg).append(" damage\n");
        return dmg;
    }

    private static Hero findHero(GameData data, String name) {
        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(name)) return h;
        }
        return null;
    }
}
