package service;

import model.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin data management: CRUD operations (in-memory, resets on restart)
 */
public class DataManager {

    // ===== Player CRUD =====
    public static void addPlayer(GameData data, String name, String rank, double winRate, int matches) {
        String id = "P" + String.format("%02d", data.getPlayers().size() + 1);
        Player p = new Player(id, name, "123", Role.PLAYER, rank, winRate, matches, null);
        data.getPlayers().add(p);
        System.out.println("Player added: " + name);
    }

    public static void removePlayer(GameData data, String name) {
        List<Player> list = data.getPlayers();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUsername().equalsIgnoreCase(name)) {
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
        String id = "H" + String.format("%02d", data.getHeroes().size() + 1);
        Hero h = new Hero(id, name, role, hp, atk, def);
        data.getHeroes().add(h);
        System.out.println("Hero added: " + name);
    }

    public static void removeHero(GameData data, String name) {
        List<Hero> list = data.getHeroes();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equalsIgnoreCase(name)) {
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
        String id = "E" + String.format("%02d", data.getEquipments().size() + 1);
        Equipment e = new Equipment(id, name, type, atk, def, hp, price);
        data.getEquipments().add(e);
        System.out.println("Equipment added: " + name);
    }

    public static void removeEquipment(GameData data, String name) {
        List<Equipment> list = data.getEquipments();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equalsIgnoreCase(name)) {
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
        String id = "T" + String.format("%02d", data.getTeams().size() + 1);
        Team t = new Team(id, name, 0, 0);
        data.getTeams().add(t);
        System.out.println("Team added: " + name);
    }

    public static void removeTeam(GameData data, String name) {
        List<Team> list = data.getTeams();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTeamName().equalsIgnoreCase(name)) {
                list.remove(i);
                System.out.println("Team deleted: " + name);
                return;
            }
        }
        System.out.println("Team not found: " + name);
    }

    public static void modifyTeam(GameData data, String name, int newWins, int newLosses) {
        for (Team t : data.getTeams()) {
            if (t.getTeamName().equalsIgnoreCase(name)) {
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
        String id = "M" + String.format("%02d", data.getMatchRecords().size() + 1);
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
