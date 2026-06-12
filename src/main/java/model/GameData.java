package model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Player> players;
    private List<Admin> admins;
    private List<Hero> heroes;
    private List<Equipment> equipments;
    private List<Team> teams;
    private List<MatchRecord> matchRecords;

    public GameData() {
        players = new ArrayList<>();
        admins = new ArrayList<>();
        heroes = new ArrayList<>();
        equipments = new ArrayList<>();
        teams = new ArrayList<>();
        matchRecords = new ArrayList<>();
    }

    public List<Player> getPlayers() { return players; }
    public List<Admin> getAdmins() { return admins; }
    public List<Hero> getHeroes() { return heroes; }
    public List<Equipment> getEquipments() { return equipments; }
    public List<Team> getTeams() { return teams; }
    public List<MatchRecord> getMatchRecords() { return matchRecords; }
}
