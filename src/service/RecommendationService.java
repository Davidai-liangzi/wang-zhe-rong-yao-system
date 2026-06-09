package service;

import model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommendation Engine: Recommends equipment and heroes
 * based on hero type, stats, and player preferences.
 * Section 10.2 Extra Credit
 */
public class RecommendationService {

    /**
     * Recommend the most suitable equipment for a given hero
     * (top N from compatible equipment sorted by role-adjusted score).
     * Formula: defense-type heroes prioritize DEF+HP, attack-type prioritize ATK.
     */
    public static void recommendEquipmentForHero(GameData data, String heroName) {
        Hero target = null;
        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(heroName)) { target = h; break; }
        }
        if (target == null) {
            System.out.println("Hero not found: " + heroName);
            return;
        }

        List<Equipment> candidates = target.getCompatibleEquipments();
        if (candidates.isEmpty()) {
            // No compatible equipment — fall back to role-based recommendation
            candidates = new ArrayList<>(data.getEquipments());
        }

        // Adjust scoring weights by hero role
        final Hero finalTarget = target;
        candidates.sort((a, b) -> Double.compare(
                equipScoreForHero(b, finalTarget),
                equipScoreForHero(a, finalTarget)
        ));

        System.out.println();
        System.out.println("========== Equipment Recommendation ==========");
        System.out.println("Hero: " + target.getName() + " [" + target.getHeroRole() + "]");
        System.out.println("Strategy: role-adjusted scoring weights");
        System.out.println("Rank  Name           Type  ATK   DEF    HP    Price  Score");
        System.out.println("----------------------------------------------------------");

        int count = 0;
        for (Equipment e : candidates) {
            if (count >= 6) break;
            double s = equipScoreForHero(e, target);
            System.out.printf(" %-4d %-16s %-6s %-5d %-5d %-5d %-6d %-7.1f\n",
                    count + 1, e.getName(), e.getType(),
                    e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(),
                    e.getPrice(), s);
            count++;
        }
        System.out.println("==============================");
    }

    /** Adjust equipment scoring weights based on hero role */
    private static double equipScoreForHero(Equipment e, Hero h) {
        double atkW = 1.0, defW = 0.8, hpW = 0.6;
        switch (h.getHeroRole()) {
            case WARRIOR:
            case ASSASSIN:
                atkW = 1.5; defW = 0.5; hpW = 0.5; break;  // Prioritize attack
            case TANK:
                atkW = 0.3; defW = 1.5; hpW = 1.2; break;   // Prioritize defense
            case MAGE:
                atkW = 1.4; defW = 0.3; hpW = 0.4; break;   // Prioritize magic
            case MARKSMAN:
                atkW = 1.3; defW = 0.4; hpW = 0.5; break;   // Prioritize output
            case SUPPORT:
                defW = 1.2; hpW = 1.0; atkW = 0.2; break;   // Prioritize survival
        }
        return e.getBonusAtk() * atkW
             + e.getBonusDef() * defW
             + e.getBonusHp() * hpW
             - e.getPrice() * 0.001;
    }

    /**
     * Recommend unowned heroes for player (based on role coverage gaps)
     * If player lacks heroes of certain role, recommend unowned heroes of that role
     */
    public static void recommendHeroesForPlayer(GameData data, String playerName) {
        Player target = null;
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(playerName)) { target = p; break; }
        }
        if (target == null) {
            System.out.println("Player not found: " + playerName);
            return;
        }

        // Collect owned roles
        Set<HeroRole> ownedRoles = new HashSet<>();
        Set<String> ownedNames = new HashSet<>();
        for (Hero h : target.getHeroPool()) {
            ownedRoles.add(h.getHeroRole());
            ownedNames.add(h.getName());
        }

        // Find missing roles
        Set<HeroRole> missingRoles = new HashSet<>(Arrays.asList(HeroRole.values()));
        missingRoles.removeAll(ownedRoles);

        // Recommend heroes of missing roles (sorted by HP+ATK total)
        List<Hero> candidates = data.getHeroes().stream()
                .filter(h -> missingRoles.contains(h.getHeroRole()))
                .filter(h -> !ownedNames.contains(h.getName()))
                .sorted((a, b) -> Integer.compare(
                        b.getHp() + b.getAtk() + b.getDef(),
                        a.getHp() + a.getAtk() + a.getDef()))
                .collect(Collectors.toList());

        System.out.println();
        System.out.println("========== Hero Recommendation ==========");
        System.out.println("Player: " + target.getUsername());
        System.out.println("Owned roles: " + ownedRoles);
        System.out.println("Missing roles: " + missingRoles);
        System.out.println();

        if (candidates.isEmpty()) {
            System.out.println("  (All roles covered!)");
        } else {
            System.out.println("Recommended heroes to fill role gaps:");
            for (Hero h : candidates) {
                System.out.println("  " + h.getName() + " [" + h.getHeroRole() + "]"
                        + " HP:" + h.getHp() + " ATK:" + h.getAtk() + " DEF:" + h.getDef()
                        + " Skills: " + String.join(", ", h.getSkills()));
            }
        }
        System.out.println("==============================");
    }
}
