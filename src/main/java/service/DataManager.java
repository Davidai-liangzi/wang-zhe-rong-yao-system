package service;

import model.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin data management: CRUD operations.
 * All methods operate through GameData's controlled mutation API
 * and return result messages instead of printing directly.
 */
public class DataManager {

    /** Generate next ID by finding max existing ID suffix + 1 */
    private static String nextId(List<? extends Identifiable> items, String prefix) {
        int max = 0;
        for (Identifiable item : items) {
            String id = item.getId();
            if (id != null && id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (max >= 99) {
            return prefix + (max + 1);
        }
        return prefix + String.format("%02d", max + 1);
    }

    // ===== Player CRUD =====
    public static String addPlayer(GameData data, String name, String rank, double winRate, int matches) {
        if (data == null) return "Error: no data available.";
        if (name == null || name.trim().isEmpty()) return "Error: player name cannot be empty.";
        if (Double.isNaN(winRate) || winRate < 0 || winRate > 100) return "Error: win rate must be 0-100.";
        if (matches < 0) return "Error: matches cannot be negative.";

        String id = nextId(data.getPlayers(), "P");
        Player p = new Player(id, name, "123", Role.PLAYER, rank, winRate, matches, null);
        data.addPlayer(p);
        return "Player added: " + name;
    }

    public static String removePlayer(GameData data, String name) {
        if (data == null) return "Error: no data available.";
        for (Player player : data.getPlayers()) {
            if (player.getUsername().equalsIgnoreCase(name)) {
                Team team = player.getTeam();
                if (team != null) team.getMembers().remove(player);
                data.removePlayer(player);
                return "Player deleted: " + name;
            }
        }
        return "Player not found: " + name;
    }

    public static String modifyPlayer(GameData data, String name, String newRank, double newWinRate, int newMatches) {
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(name)) {
                p.setRank(newRank);
                p.setWinRate(newWinRate);
                p.setMatchesPlayed(newMatches);
                return "Player modified: " + name;
            }
        }
        return "Player not found: " + name;
    }

    // ===== Hero CRUD =====
    public static String addHero(GameData data, String name, HeroRole role, int hp, int atk, int def) {
        if (data == null) return "Error: no data available.";
        if (name == null || name.trim().isEmpty()) return "Error: hero name cannot be empty.";
        if (hp < 0 || atk < 0 || def < 0) return "Error: stats cannot be negative.";

        String id = nextId(data.getHeroes(), "H");
        Hero h = new Hero(id, name, role, hp, atk, def);
        data.addHero(h);
        return "Hero added: " + name;
    }

    public static String removeHero(GameData data, String name) {
        if (data == null) return "Error: no data available.";
        for (Hero hero : data.getHeroes()) {
            if (hero.getName().equalsIgnoreCase(name)) {
                for (Player p : data.getPlayers()) p.getHeroPool().remove(hero);
                data.removeHero(hero);
                return "Hero deleted: " + name;
            }
        }
        return "Hero not found: " + name;
    }

    public static String modifyHero(GameData data, String name, HeroRole newRole, int newHp, int newAtk, int newDef) {
        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(name)) {
                h.setHeroRole(newRole);
                h.setHp(newHp);
                h.setAtk(newAtk);
                h.setDef(newDef);
                return "Hero modified: " + name;
            }
        }
        return "Hero not found: " + name;
    }

    // ===== Equipment CRUD =====
    public static String addEquipment(GameData data, String name, EquipmentType type, int atk, int def, int hp, int price) {
        if (data == null) return "Error: no data available.";
        if (name == null || name.trim().isEmpty()) return "Error: equipment name cannot be empty.";
        if (atk < 0 || def < 0 || hp < 0) return "Error: stats cannot be negative.";
        if (price < 0) return "Error: price cannot be negative.";

        String id = nextId(data.getEquipments(), "E");
        Equipment e = new Equipment(id, name, type, atk, def, hp, price);
        data.addEquipment(e);
        return "Equipment added: " + name;
    }

    public static String removeEquipment(GameData data, String name) {
        if (data == null) return "Error: no data available.";
        for (Equipment eq : data.getEquipments()) {
            if (eq.getName().equalsIgnoreCase(name)) {
                for (Hero h : data.getHeroes()) h.getCompatibleEquipments().remove(eq);
                data.removeEquipment(eq);
                return "Equipment deleted: " + name;
            }
        }
        return "Equipment not found: " + name;
    }

    public static String modifyEquipment(GameData data, String name, EquipmentType newType, int newAtk, int newDef, int newHp, int newPrice) {
        for (Equipment e : data.getEquipments()) {
            if (e.getName().equalsIgnoreCase(name)) {
                e.setType(newType);
                e.setBonusAtk(newAtk);
                e.setBonusDef(newDef);
                e.setBonusHp(newHp);
                e.setPrice(newPrice);
                return "Equipment modified: " + name;
            }
        }
        return "Equipment not found: " + name;
    }

    // ===== Team CRUD =====
    public static String addTeam(GameData data, String name) {
        if (data == null) return "Error: no data available.";
        if (name == null || name.trim().isEmpty()) return "Error: team name cannot be empty.";

        String id = nextId(data.getTeams(), "T");
        Team t = new Team(id, name, 0, 0);
        data.addTeam(t);
        return "Team added: " + name;
    }

    public static String removeTeam(GameData data, String name) {
        if (data == null) return "Error: no data available.";
        for (Team team : data.getTeams()) {
            if (team.getTeamName() != null && team.getTeamName().equalsIgnoreCase(name)) {
                for (Player member : team.getMembers()) member.setTeam(null);
                data.removeTeam(team);
                return "Team deleted: " + name;
            }
        }
        return "Team not found: " + name;
    }

    public static String modifyTeam(GameData data, String name, int newWins, int newLosses) {
        for (Team t : data.getTeams()) {
            if (t.getTeamName() != null && t.getTeamName().equalsIgnoreCase(name)) {
                t.setWins(newWins);
                t.setLosses(newLosses);
                return "Team modified: " + name;
            }
        }
        return "Team not found: " + name;
    }

    // ===== Match Record CRUD =====
    public static String addMatchRecord(GameData data, Team teamA, Team teamB, int scoreA, int scoreB, LocalDate date) {
        if (data == null) return "Error: no data available.";
        if (teamA == null || teamB == null) return "Error: both teams must be specified.";

        String id = nextId(data.getMatchRecords(), "M");
        MatchRecord m = new MatchRecord(id, teamA, teamB, scoreA, scoreB, date);
        data.addMatchRecord(m);
        return "Match record added: " + teamA.getTeamName() + " vs " + teamB.getTeamName();
    }

    public static String removeMatchRecord(GameData data, String id) {
        for (MatchRecord m : data.getMatchRecords()) {
            if (m.getId().equalsIgnoreCase(id)) {
                data.removeMatchRecord(m);
                return "Match record deleted: " + id;
            }
        }
        return "Match record not found: " + id;
    }
}
