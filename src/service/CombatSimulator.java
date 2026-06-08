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
 * - Output combat log each round
 * - Output win/loss result and stats at the end
 */
public class CombatSimulator {

    private static final Random RNG = new Random();

    /**
     * Simulate a battle between two heroes (with their respective equipment)
     */
    public static void simulate(GameData data, String heroName1, String heroName2) {
        Hero h1 = findHero(data, heroName1);
        Hero h2 = findHero(data, heroName2);
        if (h1 == null || h2 == null) {
            System.out.println("Hero not found");
            return;
        }

        // Calculate total stats (hero base + compatible equipment bonuses)
        int atk1 = h1.getAtk(), def1 = h1.getDef(), hp1 = h1.getHp();
        int atk2 = h2.getAtk(), def2 = h2.getDef(), hp2 = h2.getHp();
        boolean hasAtkEq1 = false, hasDefEq1 = false;
        boolean hasAtkEq2 = false, hasDefEq2 = false;

        for (Equipment e : h1.getCompatibleEquipments()) {
            atk1 += e.getBonusAtk();
            def1 += e.getBonusDef();
            hp1 += e.getBonusHp();
            if (e.getBonusAtk() > 0) hasAtkEq1 = true;
            if (e.getBonusDef() > 0) hasDefEq1 = true;
        }
        for (Equipment e : h2.getCompatibleEquipments()) {
            atk2 += e.getBonusAtk();
            def2 += e.getBonusDef();
            hp2 += e.getBonusHp();
            if (e.getBonusAtk() > 0) hasAtkEq2 = true;
            if (e.getBonusDef() > 0) hasDefEq2 = true;
        }

        int maxHp1 = hp1, maxHp2 = hp2;
        int turn = 0;
        String log1 = "", log2 = "";

        System.out.println();
        System.out.println("========== Combat Simulator ==========");
        System.out.printf("%-20s HP=%-6d ATK=%-4d DEF=%-4d [%s]\n",
                h1.getName(), hp1, atk1, def1, h1.getHeroRole());
        System.out.printf("%-20s HP=%-6d ATK=%-4d DEF=%-4d [%s]\n",
                h2.getName(), hp2, atk2, def2, h2.getHeroRole());
        System.out.println("-----------------------------");

        while (hp1 > 0 && hp2 > 0) {
            turn++;
            System.out.println("--- Round " + turn + " ---");

            // Hero 1 attacks Hero 2
            hp2 -= attack("  " + h1.getName(), atk1, def2, hp2, hasAtkEq1, hasDefEq2);

            if (hp2 <= 0) {
                System.out.println("  " + h2.getName() + " defeated!");
                break;
            }

            // Hero 2 attacks Hero 1
            hp1 -= attack("  " + h2.getName(), atk2, def1, hp1, hasAtkEq2, hasDefEq1);

            if (hp1 <= 0) {
                System.out.println("  " + h1.getName() + " defeated!");
                break;
            }
        }

        System.out.println("-----------------------------");
        String winner = hp1 > 0 ? h1.getName() : h2.getName();
        String loser = hp1 > 0 ? h2.getName() : h1.getName();
        int winnerRemain = hp1 > 0 ? hp1 : hp2;
        int winnerMax = hp1 > 0 ? maxHp1 : maxHp2;

        System.out.println("Result: " + winner + " wins!");
        System.out.println("HP Remaining: " + winnerRemain + "/" + winnerMax);
        System.out.println("Total Rounds: " + turn);
        System.out.println("==============================");
    }

    private static int attack(String attacker, int atk, int targetDef, int targetHp,
                               boolean hasAtkEq, boolean hasDefEq) {
        // Base damage
        int baseDmg = Math.max(1, atk - (int)(targetDef * 0.6));
        int dmg = baseDmg + RNG.nextInt(11) - 5; // ±5 random

        // Critical hit
        boolean crit = false;
        int critChance = hasAtkEq ? 20 : 15;
        if (RNG.nextInt(100) < critChance) {
            dmg = (int)(dmg * 1.5);
            crit = true;
        }

        // Dodge
        int dodgeChance = hasDefEq ? 15 : 10;
        if (RNG.nextInt(100) < dodgeChance) {
            System.out.println(attacker + " attacks → dodged! (0)");
            return 0;
        }

        dmg = Math.max(1, dmg);
        String critMsg = crit ? " CRITICAL!" : "";
        System.out.println(attacker + " attacks → " + dmg + " damage" + critMsg);
        return dmg;
    }

    private static Hero findHero(GameData data, String name) {
        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(name)) return h;
        }
        return null;
    }
}
