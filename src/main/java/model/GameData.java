package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Central data container holding all game entities.
 * Uses List<Person> users for polymorphic management of both Player and Admin.
 * All getters return unmodifiable views to enforce encapsulation.
 */
public class GameData {
    private final List<Player> players;
    private final List<Admin> admins;
    private final List<Person> users;     // polymorphic: contains all Player + Admin objects
    private final List<Hero> heroes;
    private final List<Equipment> equipments;
    private final List<Team> teams;
    private final List<MatchRecord> matchRecords;

    public GameData() {
        players = new ArrayList<>();
        admins = new ArrayList<>();
        users = new ArrayList<>();
        heroes = new ArrayList<>();
        equipments = new ArrayList<>();
        teams = new ArrayList<>();
        matchRecords = new ArrayList<>();
    }

    // ======================
    // Unmodifiable getters — enforcing encapsulation
    // ======================
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<Admin> getAdmins() { return Collections.unmodifiableList(admins); }
    public List<Person> getUsers() { return Collections.unmodifiableList(users); }
    public List<Hero> getHeroes() { return Collections.unmodifiableList(heroes); }
    public List<Equipment> getEquipments() { return Collections.unmodifiableList(equipments); }
    public List<Team> getTeams() { return Collections.unmodifiableList(teams); }
    public List<MatchRecord> getMatchRecords() { return Collections.unmodifiableList(matchRecords); }

    // ======================
    // Controlled mutation — used by DataInitializer and DataManager
    // ======================
    public void addPlayer(Player p) {
        players.add(p);
        users.add(p);
    }

    public boolean removePlayer(Player p) {
        users.remove(p);
        return players.remove(p);
    }

    public void addAdmin(Admin a) {
        admins.add(a);
        users.add(a);
    }

    public void addHero(Hero h) {
        heroes.add(h);
    }

    public boolean removeHero(Hero h) {
        return heroes.remove(h);
    }

    public void addEquipment(Equipment e) {
        equipments.add(e);
    }

    public boolean removeEquipment(Equipment e) {
        return equipments.remove(e);
    }

    public void addTeam(Team t) {
        teams.add(t);
    }

    public boolean removeTeam(Team t) {
        return teams.remove(t);
    }

    public void addMatchRecord(MatchRecord m) {
        matchRecords.add(m);
    }

    public boolean removeMatchRecord(MatchRecord m) {
        return matchRecords.remove(m);
    }
}
