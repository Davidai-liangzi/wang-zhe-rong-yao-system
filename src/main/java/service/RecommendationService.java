package service;

import model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommendation Engine: Recommends equipment and heroes
 * based on hero type, stats, and player preferences.
 * All methods return formatted String results.
 * Section 10.2 Extra Credit
 */
public class RecommendationService {

    /**
     * Recommend the most suitable equipment for a given hero.
     * Returns formatted ranking as String.
     */
    public static String recommendEquipmentForHero(GameData data, String heroName) {
        if (data == null || data.getHeroes() == null) return "Error: no data available.";

        Hero target = null;
        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(heroName)) { target = h; break; }
        }
        if (target == null) return "Hero not found: " + heroName;

        List<Equipment> candidates = target.getCompatibleEquipments();
        if (candidates.isEmpty()) candidates = new ArrayList<>(data.getEquipments());

        final Hero finalTarget = target;
        candidates.sort((a, b) -> Double.compare(
                equipScoreForHero(b, finalTarget),
                equipScoreForHero(a, finalTarget)));

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Equipment Recommendation ==========\n");
        sb.append("Hero: ").append(target.getName()).append(" [").append(target.getHeroRole()).append("]\n");
        sb.append("Strategy: role-adjusted scoring weights\n");
        sb.append(String.format("Rank  %-16s %-6s %-5s %-5s %-5s %-6s %-7s\n",
                "Name", "Type", "ATK", "DEF", "HP", "Price", "Score"));
        sb.append("----------------------------------------------------------\n");

        int count = 0;
        for (Equipment e : candidates) {
            if (count >= 6) break;
            double s = equipScoreForHero(e, target);
            sb.append(String.format(" %-4d %-16s %-6s %-5d %-5d %-5d %-6d %-7.1f\n",
                    count + 1, e.getName(), e.getType(),
                    e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(), e.getPrice(), s));
            count++;
        }
        sb.append("==============================\n");
        return sb.toString();
    }

    /** Adjust equipment scoring weights based on hero role */
    public static double equipScoreForHero(Equipment e, Hero h) {
        double atkW = 1.0, defW = 0.8, hpW = 0.6;
        switch (h.getHeroRole()) {
            case WARRIOR:
            case ASSASSIN:  atkW = 1.5; defW = 0.5; hpW = 0.5; break;
            case TANK:      atkW = 0.3; defW = 1.5; hpW = 1.2; break;
            case MAGE:      atkW = 1.4; defW = 0.3; hpW = 0.4; break;
            case MARKSMAN:  atkW = 1.3; defW = 0.4; hpW = 0.5; break;
            case SUPPORT:   defW = 1.2; hpW = 1.0; atkW = 0.2; break;
        }
        return e.getBonusAtk() * atkW + e.getBonusDef() * defW
             + e.getBonusHp() * hpW - e.getPrice() * 0.001;
    }

    /**
     * Recommend unowned heroes for player (based on role coverage gaps).
     * @return sorted recommended heroes list (for testing)
     */
    public static List<Hero> getRecommendedHeroesForPlayer(GameData data, String playerName) {
        if (data == null || data.getPlayers() == null) return new ArrayList<>();

        Player target = null;
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(playerName)) { target = p; break; }
        }
        if (target == null) return new ArrayList<>();

        Set<HeroRole> ownedRoles = new HashSet<>();
        Set<String> ownedNames = new HashSet<>();
        for (Hero h : target.getHeroPool()) {
            ownedRoles.add(h.getHeroRole());
            ownedNames.add(h.getName());
        }
        Set<HeroRole> missingRoles = new HashSet<>(Arrays.asList(HeroRole.values()));
        missingRoles.removeAll(ownedRoles);

        return data.getHeroes().stream()
                .filter(h -> missingRoles.contains(h.getHeroRole()))
                .filter(h -> !ownedNames.contains(h.getName()))
                .sorted((a, b) -> Integer.compare(
                        b.getHp() + b.getAtk() + b.getDef(),
                        a.getHp() + a.getAtk() + a.getDef()))
                .collect(Collectors.toList());
    }

    /** Returns hero recommendations as formatted String */
    public static String recommendHeroesForPlayer(GameData data, String playerName) {
        List<Hero> candidates = getRecommendedHeroesForPlayer(data, playerName);

        Player target = null;
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(playerName)) { target = p; break; }
        }
        if (target == null) return "Player not found: " + playerName;

        Set<HeroRole> ownedRoles = new HashSet<>();
        Set<String> ownedNames = new HashSet<>();
        for (Hero h : target.getHeroPool()) {
            ownedRoles.add(h.getHeroRole());
            ownedNames.add(h.getName());
        }
        Set<HeroRole> missingRoles = new HashSet<>(Arrays.asList(HeroRole.values()));
        missingRoles.removeAll(ownedRoles);

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Hero Recommendation ==========\n");
        sb.append("Player: ").append(target.getUsername()).append("\n");
        sb.append("Owned roles: ").append(ownedRoles).append("\n");
        sb.append("Missing roles: ").append(missingRoles).append("\n\n");

        if (candidates.isEmpty()) {
            sb.append("  (All roles covered!)\n");
        } else {
            sb.append("Recommended heroes to fill role gaps:\n");
            for (Hero h : candidates) {
                sb.append("  ").append(h.getName()).append(" [").append(h.getHeroRole()).append("]")
                  .append(" HP:").append(h.getHp()).append(" ATK:").append(h.getAtk()).append(" DEF:").append(h.getDef())
                  .append(" Skills: ").append(String.join(", ", h.getSkills())).append("\n");
            }
        }
        sb.append("==============================\n");
        return sb.toString();
    }
}
