package service;

import model.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin data management: CRUD operations (in-memory, resets on restart)
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
    public static void addPlayer(GameData data, String name, String rank, double winRate, int matches) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: player name cannot be empty.");
            return;
        }
        if (Double.isNaN(winRate) || winRate < 0 || winRate > 100) {
            System.out.println("Error: win rate must be 0-100.");
            return;
        }
        if (matches < 0) {
            System.out.println("Error: matches cannot be negative.");
            return;
        }
        String id = nextId(data.getPlayers(), "P");
        Player p = new Player(id, name, "123", Role.PLAYER, rank, winRate, matches, null);
        data.getPlayers().add(p);
        System.out.println("Player added: " + name);
    }

    public static void removePlayer(GameData data, String name) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        List<Player> list = data.getPlayers();
        for (int i = 0; i < list.size(); i++) {
            Player player = list.get(i);
            if (player.getUsername().equalsIgnoreCase(name)) {
                // Cascading cleanup: remove from team members
                Team team = player.getTeam();
                if (team != null) {
                    team.getMembers().remove(player);
                }
                list.remove(i);
                System.out.println("Player deleted: " + name);
                return;
            }
        }
        System.out.println("Player not found: " + name);
    }

    public static void modifyPlayer(GameData data, String name, String newRank, double newWinRate, int newMatches) {
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(name)) {
                p.setRank(newRank);
                p.setWinRate(newWinRate);
                p.setMatchesPlayed(newMatches);
                System.out.println("Player modified: " + name);
                return;
            }
        }
        System.out.println("Player not found: " + name);
    }

    // ===== Hero CRUD =====
    public static void addHero(GameData data, String name, HeroRole role, int hp, int atk, int def) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: hero name cannot be empty.");
            return;
        }
        if (hp < 0 || atk < 0 || def < 0) {
            System.out.println("Error: stats cannot be negative.");
            return;
        }
        String id = nextId(data.getHeroes(), "H");
        Hero h = new Hero(id, name, role, hp, atk, def);
        data.getHeroes().add(h);
        System.out.println("Hero added: " + name);
    }

    public static void removeHero(GameData data, String name) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        List<Hero> list = data.getHeroes();
        for (int i = 0; i < list.size(); i++) {
            Hero hero = list.get(i);
            if (hero.getName().equalsIgnoreCase(name)) {
                // Cascading cleanup: remove hero from all players' hero pools
                for (Player p : data.getPlayers()) {
                    p.getHeroPool().remove(hero);
                }
                list.remove(i);
                System.out.println("Hero deleted: " + name);
                return;
            }
        }
        System.out.println("Hero not found: " + name);
    }

    public static void modifyHero(GameData data, String name, HeroRole newRole, int newHp, int newAtk, int newDef) {
        for (Hero h : data.getHeroes()) {
            if (h.getName().equalsIgnoreCase(name)) {
                h.setHeroRole(newRole);
                h.setHp(newHp);
                h.setAtk(newAtk);
                h.setDef(newDef);
                System.out.println("Hero modified: " + name);
                return;
            }
        }
        System.out.println("Hero not found: " + name);
    }

    // ===== Equipment CRUD =====
    public static void addEquipment(GameData data, String name, EquipmentType type, int atk, int def, int hp, int price) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: equipment name cannot be empty.");
            return;
        }
        if (atk < 0 || def < 0 || hp < 0) {
            System.out.println("Error: stats cannot be negative.");
            return;
        }
        if (price < 0) {
            System.out.println("Error: price cannot be negative.");
            return;
        }
        String id = nextId(data.getEquipments(), "E");
        Equipment e = new Equipment(id, name, type, atk, def, hp, price);
        data.getEquipments().add(e);
        System.out.println("Equipment added: " + name);
    }

    public static void removeEquipment(GameData data, String name) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        List<Equipment> list = data.getEquipments();
        for (int i = 0; i < list.size(); i++) {
            Equipment eq = list.get(i);
            if (eq.getName().equalsIgnoreCase(name)) {
                // Cascading cleanup: remove equipment from all heroes' compatible lists
                for (Hero h : data.getHeroes()) {
                    h.getCompatibleEquipments().remove(eq);
                }
                list.remove(i);
                System.out.println("Equipment deleted: " + name);
                return;
            }
        }
        System.out.println("Equipment not found: " + name);
    }

    public static void modifyEquipment(GameData data, String name, EquipmentType newType, int newAtk, int newDef, int newHp, int newPrice) {
        for (Equipment e : data.getEquipments()) {
            if (e.getName().equalsIgnoreCase(name)) {
                e.setType(newType);
                e.setBonusAtk(newAtk);
                e.setBonusDef(newDef);
                e.setBonusHp(newHp);
                e.setPrice(newPrice);
                System.out.println("Equipment modified: " + name);
                return;
            }
        }
        System.out.println("Equipment not found: " + name);
    }

    // ===== Team CRUD =====
    public static void addTeam(GameData data, String name) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: team name cannot be empty.");
            return;
        }
        String id = nextId(data.getTeams(), "T");
        Team t = new Team(id, name, 0, 0);
        data.getTeams().add(t);
        System.out.println("Team added: " + name);
    }

    public static void removeTeam(GameData data, String name) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        List<Team> list = data.getTeams();
        for (int i = 0; i < list.size(); i++) {
            Team team = list.get(i);
            if (team.getTeamName() != null && team.getTeamName().equalsIgnoreCase(name)) {
                // Cascading cleanup: null out team reference on all members
                for (Player member : team.getMembers()) {
                    member.setTeam(null);
                }
                list.remove(i);
                System.out.println("Team deleted: " + name);
                return;
            }
        }
        System.out.println("Team not found: " + name);
    }

    public static void modifyTeam(GameData data, String name, int newWins, int newLosses) {
        for (Team t : data.getTeams()) {
            if (t.getTeamName() != null && t.getTeamName().equalsIgnoreCase(name)) {
                t.setWins(newWins);
                t.setLosses(newLosses);
                System.out.println("Team modified: " + name);
                return;
            }
        }
        System.out.println("Team not found: " + name);
    }

    // ===== Match Record CRUD =====
    public static void addMatchRecord(GameData data, Team teamA, Team teamB, int scoreA, int scoreB, LocalDate date) {
        if (data == null) {
            System.out.println("Error: no data available.");
            return;
        }
        if (teamA == null || teamB == null) {
            System.out.println("Error: both teams must be specified.");
            return;
        }
        String id = nextId(data.getMatchRecords(), "M");
        MatchRecord m = new MatchRecord(id, teamA, teamB, scoreA, scoreB, date);
        data.getMatchRecords().add(m);
        System.out.println("Match record added: " + teamA.getTeamName() + " vs " + teamB.getTeamName());
    }

    public static void removeMatchRecord(GameData data, String id) {
        List<MatchRecord> list = data.getMatchRecords();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equalsIgnoreCase(id)) {
                list.remove(i);
                System.out.println("Match record deleted: " + id);
                return;
            }
        }
        System.out.println("Match record not found: " + id);
    }
}
