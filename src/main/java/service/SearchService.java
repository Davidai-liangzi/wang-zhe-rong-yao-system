package service;

import model.*;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Implements Searchable interface for all query operations.
 * All methods return formatted String results instead of printing.
 * Static helper methods exposed for testing convenience.
 */
public class SearchService implements Searchable {

    /** Find player by ID or username (case-insensitive) */
    @Override
    public String findPlayerByName(GameData data, String name) {
        if (data == null || data.getPlayers() == null) return "Error: no data available.";
        if (name == null) return "Player not found: (null input)";

        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(name) || p.getId().equalsIgnoreCase(name)) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n========== Player Info ==========\n");
                sb.append("Username: ").append(p.getUsername()).append("\n");
                sb.append("Rank: ").append(p.getRank()).append("\n");
                sb.append("Win Rate: ").append(p.getWinRate()).append("%\n");
                sb.append("Matches Played: ").append(p.getMatchesPlayed()).append("\n");

                Team team = p.getTeam();
                sb.append("Team: ").append(team != null ? team.getTeamName() : "None").append("\n");

                sb.append("--- Hero Pool ---\n");
                List<Hero> heroPool = p.getHeroPool();
                if (heroPool.isEmpty()) {
                    sb.append("  (No heroes)\n");
                } else {
                    for (Hero h : heroPool) {
                        sb.append("  ").append(h.getName()).append(" [").append(h.getHeroRole()).append("]")
                          .append(" HP:").append(h.getHp()).append(" ATK:").append(h.getAtk()).append(" DEF:").append(h.getDef()).append("\n");
                        if (!h.getSkills().isEmpty()) {
                            sb.append("    Skills: ").append(String.join(", ", h.getSkills())).append("\n");
                        }
                        if (!h.getCompatibleEquipments().isEmpty()) {
                            List<String> eqNames = new ArrayList<>();
                            for (Equipment e : h.getCompatibleEquipments()) eqNames.add(e.getName());
                            sb.append("    Equipment: ").append(String.join(", ", eqNames)).append("\n");
                        }
                    }
                }
                sb.append("==============================\n");
                return sb.toString();
            }
        }
        return "Player not found: " + name;
    }

    /** Find team by name, display members and stats */
    @Override
    public String findTeamByName(GameData data, String name) {
        if (data == null || data.getTeams() == null) return "Error: no data available.";
        if (name == null) return "Team not found: (null input)";

        for (Team t : data.getTeams()) {
            if (t.getTeamName() == null) continue;
            if (t.getTeamName().equalsIgnoreCase(name) || t.getId().equalsIgnoreCase(name) || t.getTeamName().contains(name)) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n========== Team Info ==========\n");
                sb.append("Team: ").append(t.getTeamName()).append("\n");
                sb.append("Record: ").append(t.getWins()).append("W / ").append(t.getLosses()).append("L\n");
                double wr = t.getWins() + t.getLosses() > 0
                        ? (double) t.getWins() / (t.getWins() + t.getLosses()) * 100 : 0;
                sb.append(String.format("Win Rate: %.1f%%\n", wr));
                sb.append("Members: ").append(t.getMembers().size()).append("\n");
                sb.append("--- Member List ---\n");

                int totalMatches = 0;
                double totalRankScore = 0;
                for (Player m : t.getMembers()) {
                    sb.append("  ").append(m.getUsername()).append(" | Rank: ").append(m.getRank())
                      .append(" | WR: ").append(m.getWinRate()).append("% | Matches: ").append(m.getMatchesPlayed()).append("\n");
                    totalMatches += m.getMatchesPlayed();
                    totalRankScore += rankToScore(m.getRank());
                }

                sb.append("--- Stats ---\n");
                sb.append("Total Matches: ").append(totalMatches).append("\n");
                double avgRankScore = t.getMembers().isEmpty() ? 0 : totalRankScore / t.getMembers().size();
                sb.append("Average Rank: ").append(scoreToRankName(avgRankScore)).append("\n");

                Player top = t.getMembers().stream()
                        .max(Comparator.comparingDouble(Player::getWinRate))
                        .orElse(null);
                if (top != null) {
                    sb.append("Top Player: ").append(top.getUsername()).append(" (WR ").append(top.getWinRate()).append("%)\n");
                }
                sb.append("==============================\n");
                return sb.toString();
            }
        }
        return "Team not found: " + name;
    }

    /** Find hero by name, display attributes and owners */
    @Override
    public String findHeroByName(GameData data, String name) {
        if (data == null || data.getHeroes() == null) return "Error: no data available.";
        if (name == null) return "Hero not found: (null input)";

        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(name)) return buildHeroDetails(data, h);
        }
        if (!name.isEmpty()) {
            for (Hero h : data.getHeroes()) {
                if (h.getName().contains(name)) return buildHeroDetails(data, h);
            }
        }
        return "Hero not found: " + name;
    }

    private String buildHeroDetails(GameData data, Hero h) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Hero Details ==========\n");
        sb.append("Name: ").append(h.getName()).append("\n");
        sb.append("Role: ").append(h.getHeroRole()).append("\n");
        sb.append("Base Stats: HP=").append(h.getHp()).append(" ATK=").append(h.getAtk()).append(" DEF=").append(h.getDef()).append("\n");

        sb.append("Skills: ");
        if (!h.getSkills().isEmpty()) sb.append(String.join(", ", h.getSkills())).append("\n");
        else sb.append("(none)\n");

        sb.append("--- Compatible Equipment ---\n");
        List<Equipment> compEqs = h.getCompatibleEquipments();
        if (compEqs.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (Equipment e : compEqs) {
                sb.append(String.format("  %s [%s] ATK+%d DEF+%d HP+%d Price: %d\n",
                        e.getName(), e.getType(), e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(), e.getPrice()));
            }
        }

        sb.append("--- Recommended Equipment ---\n");
        boolean foundEq = false;
        for (Equipment e : data.getEquipments()) {
            if (isSuitable(e.getType(), h.getHeroRole())) {
                sb.append(String.format("  %s [%s] ATK+%d DEF+%d HP+%d Price: %d\n",
                        e.getName(), e.getType(), e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(), e.getPrice()));
                foundEq = true;
            }
        }
        if (!foundEq) sb.append("  (no matching equipment)\n");

        sb.append("--- Owners ---\n");
        boolean foundPlayer = false;
        for (Player p : data.getPlayers()) {
            for (Hero pHero : p.getHeroPool()) {
                if (pHero.getName().equals(h.getName())) {
                    sb.append("  ").append(p.getUsername()).append(" | Rank: ").append(p.getRank())
                      .append(" | WR: ").append(p.getWinRate()).append("% | Team: ")
                      .append(p.getTeam() != null ? p.getTeam().getTeamName() : "-").append("\n");
                    foundPlayer = true;
                    break;
                }
            }
        }
        if (!foundPlayer) sb.append("  (no owners)\n");
        sb.append("==============================\n");
        return sb.toString();
    }

    /** Equipment ranking by composite score */
    @Override
    public String showEquipmentRanking(GameData data) {
        if (data == null || data.getEquipments() == null) return "Error: no data available.";

        List<Equipment> list = new ArrayList<>(data.getEquipments());
        list.sort((a, b) -> Double.compare(equipScore(b), equipScore(a)));

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Equipment Ranking ==========\n");
        sb.append("Formula: score = ATK x 1.0 + DEF x 0.8 + HP x 0.6 - Price x 0.001\n");
        sb.append(String.format("%-4s %-16s %-8s %-5s %-5s %-6s %-6s %-8s\n",
                "Rank", "Name", "Type", "ATK", "DEF", "HP", "Price", "Score"));
        sb.append("--------------------------------------------------------------\n");

        int rank = 1;
        for (Equipment e : list) {
            sb.append(String.format(" %-3d %-16s %-8s %-5d %-5d %-6d %-6d %-8.1f\n",
                    rank++, e.getName(), e.getType(),
                    e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(),
                    e.getPrice(), equipScore(e)));
        }
        sb.append("==============================\n");
        return sb.toString();
    }

    /** Player leaderboard */
    @Override
    public String showLeaderboard(GameData data) {
        if (data == null || data.getPlayers() == null) return "Error: no data available.";

        List<Player> list = new ArrayList<>(data.getPlayers());
        list.sort((a, b) -> {
            int cmp = Double.compare(playerScore(b), playerScore(a));
            if (cmp == 0) cmp = a.getUsername().compareTo(b.getUsername());
            return cmp;
        });

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Leaderboard ==========\n");
        sb.append(String.format("%-4s %-14s %-6s %-6s %-6s %-8s\n",
                "Rank", "Username", "Rank", "WR%", "Mts", "Score"));
        sb.append("--------------------------------------------------\n");

        int rank = 1;
        for (Player p : list) {
            sb.append(String.format(" %-3d %-14s %-6s %-6.1f %-6d %-8.1f\n",
                    rank++, p.getUsername(), p.getRank(),
                    p.getWinRate(), p.getMatchesPlayed(), playerScore(p)));
        }
        sb.append("==============================\n");
        return sb.toString();
    }

    /** Match history — build team lookup and match results */
    @Override
    public String showMatchHistory(GameData data, String input) {
        if (data == null || data.getPlayers() == null) return "Error: no data available.";
        if (input == null) return "No team found for: (null input)";

        Team targetTeam = findTargetTeam(data, input);
        if (targetTeam == null) return "No team found for: " + input;

        return buildMatchHistory(data, targetTeam, input);
    }

    private Team findTargetTeam(GameData data, String input) {
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(input)) return p.getTeam();
        }
        for (Team t : data.getTeams()) {
            if (t.getTeamName() != null && (t.getTeamName().equalsIgnoreCase(input) || t.getTeamName().contains(input)))
                return t;
        }
        return null;
    }

    private String buildMatchHistory(GameData data, Team team, String querySource) {
        StringBuilder sb = new StringBuilder();
        sb.append("Query: ").append(querySource).append("\n");

        List<MatchRecord> matches = data.getMatchRecords().stream()
                .filter(m -> m.getTeamA().equals(team) || m.getTeamB().equals(team))
                .sorted((a, b) -> b.getMatchDate().compareTo(a.getMatchDate()))
                .limit(5)
                .collect(Collectors.toList());

        if (matches.isEmpty()) return sb.append("No match history.\n").toString();

        sb.append("\n========== Match History ==========\n");
        java.util.Map<String, Integer> pickCount = new java.util.HashMap<>();
        for (MatchRecord m : matches) {
            Team opponent = m.getTeamA().equals(team) ? m.getTeamB() : m.getTeamA();
            boolean isTeamA = m.getTeamA().equals(team);
            int myScore = isTeamA ? m.getScoreA() : m.getScoreB();
            int oppScore = isTeamA ? m.getScoreB() : m.getScoreA();
            String result = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

            sb.append("Date: ").append(m.getMatchDate()).append(" | vs ").append(opponent.getTeamName())
              .append(" | ").append(myScore).append(":").append(oppScore).append(" | ").append(result).append("\n");
            sb.append("  Participating Heroes: ");
            for (Player member : team.getMembers()) {
                if (!member.getHeroPool().isEmpty()) {
                    String heroName = member.getHeroPool().get(0).getName();
                    sb.append(heroName).append(" ");
                    pickCount.put(heroName, pickCount.getOrDefault(heroName, 0) + 1);
                }
            }
            sb.append("\n");
        }

        sb.append("--- Hero Pick Rate ---\n");
        int totalMatches = matches.size();
        if (totalMatches > 0 && !pickCount.isEmpty()) {
            sb.append(String.format("%-20s %-6s %-8s\n", "Hero", "Picks", "Rate"));
            sb.append("-------------------------------------\n");
            for (java.util.Map.Entry<String, Integer> entry : pickCount.entrySet()) {
                sb.append(String.format("%-20s %-6d %-8.1f%%\n",
                        entry.getKey(), entry.getValue(), entry.getValue() * 100.0 / totalMatches));
            }
        }
        sb.append("==============================\n");
        return sb.toString();
    }

    // ======================
    // Static utility helpers (exposed for testing)
    // ======================

    public static double equipScore(Equipment e) {
        return e.getBonusAtk() * 1.0 + e.getBonusDef() * 0.8 + e.getBonusHp() * 0.6 - e.getPrice() * 0.001;
    }

    public static double playerScore(Player p) {
        return p.getWinRate() * 1.0 + rankToScore(p.getRank()) * 5.0 + p.getMatchesPlayed() * 0.01;
    }

    public static int rankToScore(String rank) {
        if (rank == null) return 1;
        rank = rank.trim();
        if (rank.equalsIgnoreCase("King")) return 5;
        if (rank.equalsIgnoreCase("Star") || rank.equalsIgnoreCase("Star Glory")) return 4;
        if (rank.equalsIgnoreCase("Diamond")) return 3;
        if (rank.equalsIgnoreCase("Platinum")) return 2;
        if (rank.equalsIgnoreCase("Gold")) return 1;
        return 1;
    }

    public static String scoreToRankName(double score) {
        if (score >= 4.5) return "King";
        if (score >= 3.5) return "Star";
        if (score >= 2.5) return "Diamond";
        if (score >= 1.5) return "Platinum";
        return "Gold";
    }

    public static boolean isSuitable(EquipmentType type, HeroRole role) {
        switch (role) {
            case WARRIOR:
                return type == EquipmentType.ATTACK || type == EquipmentType.JUNGLE || type == EquipmentType.MOVEMENT;
            case ASSASSIN:
                return type == EquipmentType.ATTACK || type == EquipmentType.JUNGLE || type == EquipmentType.MOVEMENT;
            case MAGE:
                return type == EquipmentType.MAGIC || type == EquipmentType.MOVEMENT;
            case TANK:
                return type == EquipmentType.DEFENSE || type == EquipmentType.MOVEMENT;
            case MARKSMAN:
                return type == EquipmentType.ATTACK || type == EquipmentType.MOVEMENT;
            case SUPPORT:
                return type == EquipmentType.DEFENSE || type == EquipmentType.MOVEMENT || type == EquipmentType.MAGIC;
            default:
                return false;
        }
    }
}
