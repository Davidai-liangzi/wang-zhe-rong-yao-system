package model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Team implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String teamName;
    private List<Player> members;
    private int wins;
    private int losses;

    public Team() {
        this.members = new ArrayList<>();
    }

    public Team(String id, String teamName, int wins, int losses) {
        this.id = id;
        this.teamName = teamName;
        this.members = new ArrayList<>();
        this.wins = wins;
        this.losses = losses;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return teamName; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public List<Player> getMembers() { return members; }
    public void setMembers(List<Player> members) { this.members = members; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return java.util.Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
