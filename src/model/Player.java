package model;

import java.util.List;
import java.util.ArrayList;

public class Player extends Person {
    private List<Hero> heroPool;
    private String rank;
    private double winRate;
    private int matchesPlayed;
    private Team team;

    public Player() {
        this.heroPool = new ArrayList<>();
    }

    public Player(String id, String username, String password, Role role,
                  String rank, double winRate, int matchesPlayed, Team team) {
        super(id, username, password, role);
        this.heroPool = new ArrayList<>();
        this.rank = rank;
        this.winRate = winRate;
        this.matchesPlayed = matchesPlayed;
        this.team = team;
    }

    public List<Hero> getHeroPool() { return heroPool; }
    public void setHeroPool(List<Hero> heroPool) { this.heroPool = heroPool; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
