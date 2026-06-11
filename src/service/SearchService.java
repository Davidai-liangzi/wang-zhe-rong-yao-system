package service;

import model.*;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SearchService {

    /** Find player by username (case-insensitive) */
    public static void findPlayerByName(GameData data, String name) {
        if (data == null || data.getPlayers() == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null) {
            System.out.println("Player not found: (null input)");
            return;
        }
        List<Player> players = data.getPlayers();

        for (Player p : players) {
            if (p.getUsername().equalsIgnoreCase(name)) {
                System.out.println();
                System.out.println("========== Player Info ==========");
                System.out.println("Username: " + p.getUsername());
                System.out.println("Rank: " + p.getRank());
                System.out.println("Win Rate: " + p.getWinRate() + "%");
                System.out.println("Matches Played: " + p.getMatchesPlayed());

                Team team = p.getTeam();
                if (team != null) {
                    System.out.println("Team: " + team.getTeamName());
                } else {
                    System.out.println("Team: None");
                }

                System.out.println("--- Hero Pool ---");
                List<Hero> heroPool = p.getHeroPool();
                if (heroPool.isEmpty()) {
                    System.out.println("  (No heroes)");
                } else {
                    for (Hero h : heroPool) {
                        System.out.println("  " + h.getName() + " [" + h.getHeroRole() + "]"
                                + " HP:" + h.getHp() + " ATK:" + h.getAtk() + " DEF:" + h.getDef());
                        if (!h.getSkills().isEmpty()) {
                            System.out.println("    Skills: " + String.join(", ", h.getSkills()));
                        }
                        if (!h.getCompatibleEquipments().isEmpty()) {
                            List<String> eqNames = new ArrayList<>();
                            for (Equipment e : h.getCompatibleEquipments()) {
                                eqNames.add(e.getName());
                            }
                            System.out.println("    Equipment: " + String.join(", ", eqNames));
                        }
                    }
                }
                System.out.println("==============================");
                return;
            }
        }

        System.out.println("Player not found: " + name);
    }

    /** Find team by name, display members, average rank, total matches, win rate, top player */
    public static void findTeamByName(GameData data, String name) {
        if (data == null || data.getTeams() == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null) {
            System.out.println("Team not found: (null input)");
            return;
        }
        List<Team> teams = data.getTeams();

        for (Team t : teams) {
            if (t.getTeamName() == null) continue;
            if (t.getTeamName().equalsIgnoreCase(name) || t.getTeamName().contains(name)) {
                System.out.println();
                System.out.println("========== Team Info ==========");
                System.out.println("Team: " + t.getTeamName());
                System.out.println("Record: " + t.getWins() + "W / " + t.getLosses() + "L");
                double wr = t.getWins() + t.getLosses() > 0
                        ? (double) t.getWins() / (t.getWins() + t.getLosses()) * 100 : 0;
                System.out.printf("Win Rate: %.1f%%\n", wr);
                System.out.println("Members: " + t.getMembers().size());
                System.out.println("--- Member List ---");

                int totalMatches = 0;
                double totalRankScore = 0;
                for (Player m : t.getMembers()) {
                    System.out.println("  " + m.getUsername() + " | Rank: " + m.getRank()
                            + " | WR: " + m.getWinRate() + "% | Matches: " + m.getMatchesPlayed());
                    totalMatches += m.getMatchesPlayed();
                    totalRankScore += rankToScore(m.getRank());
                }

                System.out.println("--- Stats ---");
                System.out.println("Total Matches: " + totalMatches);
                double avgRankScore = t.getMembers().isEmpty() ? 0 : totalRankScore / t.getMembers().size();
                System.out.println("Average Rank: " + scoreToRankName(avgRankScore));

                Player top = t.getMembers().stream()
                        .max(Comparator.comparingDouble(Player::getWinRate))
                        .orElse(null);
                if (top != null) {
                    System.out.println("Top Player: " + top.getUsername() + " (WR " + top.getWinRate() + "%)");
                }
                System.out.println("==============================");
                return;
            }
        }

        System.out.println("Team not found: " + name);
    }

    /** Find hero by name, display attributes, compatible/recommended equipment, owners */
    public static void findHeroByName(GameData data, String name) {
        if (data == null || data.getHeroes() == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null) {
            System.out.println("Hero not found: (null input)");
            return;
        }
        List<Hero> heroes = data.getHeroes();

        for (Hero h : heroes) {
            if (h.getName().equalsIgnoreCase(name) || h.getName().contains(name)) {
                System.out.println();
                System.out.println("========== Hero Details ==========");
                System.out.println("Name: " + h.getName());
                System.out.println("Role: " + h.getHeroRole());
                System.out.println("Base Stats: HP=" + h.getHp() + " ATK=" + h.getAtk() + " DEF=" + h.getDef());

                if (!h.getSkills().isEmpty()) {
                    System.out.println("Skills: " + String.join(", ", h.getSkills()));
                } else {
                    System.out.println("Skills: (none)");
                }

                // Compatible equipment (directly linked to hero)
                System.out.println("--- Compatible Equipment ---");
                List<Equipment> compEqs = h.getCompatibleEquipments();
                if (compEqs.isEmpty()) {
                    System.out.println("  (none)");
                } else {
                    for (Equipment e : compEqs) {
                        System.out.println("  " + e.getName()
                                + " [" + e.getType() + "]"
                                + " ATK+" + e.getBonusAtk()
                                + " DEF+" + e.getBonusDef()
                                + " HP+" + e.getBonusHp()
                                + " Price: " + e.getPrice());
                    }
                }

                // Recommended equipment (filtered by hero role)
                System.out.println("--- Recommended Equipment ---");
                List<Equipment> eqs = data.getEquipments();
                boolean foundEq = false;
                for (Equipment e : eqs) {
                    if (isSuitable(e.getType(), h.getHeroRole())) {
                        System.out.println("  " + e.getName()
                                + " [" + e.getType() + "]"
                                + " ATK+" + e.getBonusAtk()
                                + " DEF+" + e.getBonusDef()
                                + " HP+" + e.getBonusHp()
                                + " Price: " + e.getPrice());
                        foundEq = true;
                    }
                }
                if (!foundEq) {
                    System.out.println("  (no matching equipment)");
                }

                // Players who own this hero
                System.out.println("--- Owners ---");
                List<Player> players = data.getPlayers();
                boolean foundPlayer = false;
                for (Player p : players) {
                    for (Hero pHero : p.getHeroPool()) {
                        if (pHero.getName().equals(h.getName())) {
                            System.out.println("  " + p.getUsername() + " | Rank: " + p.getRank()
                                    + " | WR: " + p.getWinRate() + "% | Team: " + (p.getTeam() != null ? p.getTeam().getTeamName() : "-"));
                            foundPlayer = true;
                            break;
                        }
                    }
                }
                if (!foundPlayer) {
                    System.out.println("  (no owners)");
                }
                System.out.println("==============================");
                return;
            }
        }

        System.out.println("Hero not found: " + name);
    }

    /** Equipment ranking by composite score */
    public static void showEquipmentRanking(GameData data) {
        if (data == null || data.getEquipments() == null) {
            System.out.println("Error: no data available.");
            return;
        }
        List<Equipment> list = new ArrayList<>(data.getEquipments());
        list.sort((a, b) -> Double.compare(equipScore(b), equipScore(a)));

        System.out.println();
        System.out.println("========== Equipment Ranking ==========");
        System.out.println("Formula: score = ATK x 1.0 + DEF x 0.8 + HP x 0.6 - Price x 0.001");
        System.out.printf("%-4s %-16s %-8s %-5s %-5s %-6s %-6s %-8s\n",
                "Rank", "Name", "Type", "ATK", "DEF", "HP", "Price", "Score");
        System.out.println("--------------------------------------------------------------");

        int rank = 1;
        for (Equipment e : list) {
            System.out.printf(" %-3d %-16s %-8s %-5d %-5d %-6d %-6d %-8.1f\n",
                    rank++, e.getName(), e.getType(),
                    e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(),
                    e.getPrice(), equipScore(e));
        }
        System.out.println("==============================");
    }

    private static double equipScore(Equipment e) {
        return e.getBonusAtk() * 1.0 + e.getBonusDef() * 0.8 + e.getBonusHp() * 0.6 - e.getPrice() * 0.001;
    }

    /** Player leaderboard */
    public static void showLeaderboard(GameData data) {
        if (data == null || data.getPlayers() == null) {
            System.out.println("Error: no data available.");
            return;
        }
        List<Player> list = new ArrayList<>(data.getPlayers());
        list.sort((a, b) -> {
            int cmp = Double.compare(playerScore(b), playerScore(a));
            if (cmp == 0) cmp = a.getUsername().compareTo(b.getUsername());
            return cmp;
        });

        System.out.println();
        System.out.println("========== Leaderboard ==========");
        System.out.printf("%-4s %-14s %-6s %-6s %-6s %-8s\n",
                "Rank", "Username", "Rank", "WR%", "Mts", "Score");
        System.out.println("--------------------------------------------------");

        int rank = 1;
        for (Player p : list) {
            System.out.printf(" %-3d %-14s %-6s %-6.1f %-6d %-8.1f\n",
                    rank++, p.getUsername(), p.getRank(),
                    p.getWinRate(), p.getMatchesPlayed(), playerScore(p));
        }
        System.out.println("==============================");
    }

    private static double playerScore(Player p) {
        return p.getWinRate() * 1.0 + rankToScore(p.getRank()) * 5.0 + p.getMatchesPlayed() * 0.01;
    }

    /** Match history — last 5 matches for a player or team */
    public static void showMatchHistory(GameData data, String input) {
        if (data == null || data.getPlayers() == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (input == null) {
            System.out.println("No team found for: (null input)");
            return;
        }
        Team targetTeam = null;

        // Try finding by player first
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(input)) {
                targetTeam = p.getTeam();
                System.out.println("Query: Player " + p.getUsername());
                if (targetTeam == null) {
                    System.out.println("This player does not belong to any team. No match history.");
                    return;
                }
                break;
            }
        }
        // If not found, try by team name
        if (targetTeam == null) {
            for (Team t : data.getTeams()) {
                if (t.getTeamName() == null) continue;
                if (t.getTeamName().equalsIgnoreCase(input) || t.getTeamName().contains(input)) {
                    targetTeam = t;
                    System.out.println("Query: Team " + t.getTeamName());
                    break;
                }
            }
        }

        if (targetTeam == null) {
            System.out.println("No team found for: " + input);
            return;
        }

        final Team team = targetTeam;
        List<MatchRecord> matches = data.getMatchRecords().stream()
                .filter(m -> m.getTeamA().equals(team) || m.getTeamB().equals(team))
                .sorted((a, b) -> b.getMatchDate().compareTo(a.getMatchDate()))
                .limit(5)
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            System.out.println("No match history.");
            return;
        }

        System.out.println();
        System.out.println("========== Match History ==========");
        for (MatchRecord m : matches) {
            Team opponent = m.getTeamA().equals(team) ? m.getTeamB() : m.getTeamA();
            boolean isTeamA = m.getTeamA().equals(team);
            int myScore = isTeamA ? m.getScoreA() : m.getScoreB();
            int oppScore = isTeamA ? m.getScoreB() : m.getScoreA();
            String result = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

            System.out.println("Date: " + m.getMatchDate() + " | vs " + opponent.getTeamName()
                    + " | " + myScore + ":" + oppScore + " | " + result);
            System.out.print("  Participating Heroes: ");
            for (Player member : team.getMembers()) {
                if (!member.getHeroPool().isEmpty()) {
                    System.out.print(member.getHeroPool().get(0).getName() + " ");
                }
            }
            System.out.println();
        }
        System.out.println("==============================");
    }

    // ==== Helpers ====

    static int rankToScore(String rank) {
        if (rank == null) return 1;
        switch (rank) {
            case "King": return 5;
            case "Star": return 4;
            case "Diamond": return 3;
            case "Platinum": return 2;
            case "Gold": return 1;
            default: return 1;
        }
    }

    static String scoreToRankName(double score) {
        if (score >= 4.5) return "King";
        if (score >= 3.5) return "Diamond+";
        if (score >= 2.5) return "Diamond";
        if (score >= 1.5) return "Platinum";
        return "Gold";
    }

    static boolean isSuitable(EquipmentType type, HeroRole role) {
        switch (role) {
            case WARRIOR:
            case ASSASSIN:
                return type == EquipmentType.ATTACK || type == EquipmentType.JUNGLE || type == EquipmentType.MOVEMENT;
            case MAGE:
                return type == EquipmentType.MAGIC || type == EquipmentType.MOVEMENT;
            case TANK:
                return type == EquipmentType.DEFENSE || type == EquipmentType.MOVEMENT;
            case MARKSMAN:
                return type == EquipmentType.ATTACK || type == EquipmentType.JUNGLE || type == EquipmentType.MOVEMENT;
            case SUPPORT:
                return type == EquipmentType.DEFENSE || type == EquipmentType.MAGIC || type == EquipmentType.MOVEMENT;
            default:
                return true;
        }
    }
}
